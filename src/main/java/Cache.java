import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cache<KEY, VALUE> {
    private final Map<KEY, CacheValue<VALUE>> cache;
    private final long ttlInMillis;
    private final int maxSize;
    private final AtomicInteger cacheSize;
    private final EvictionPolicy<KEY> evictionPolicy;
    private final ReentrantReadWriteLock cacheLock;

    public Cache(long ttlInMillis, EvictionPolicyType policyType, int maxSize) {
        this.cache = new ConcurrentHashMap<>();
        this.ttlInMillis = ttlInMillis;
        this.evictionPolicy = policyType.evictionPolicy();
        this.maxSize = maxSize;
        this.cacheSize = new AtomicInteger();
        this.cacheLock = new ReentrantReadWriteLock();

        startScheduledThread();
    }

    public Optional<VALUE> get(KEY key) {
        cacheLock.readLock().lock();
        try {
            return Optional.ofNullable(cache.compute(key, (k, value) -> {
                if (value != null) {
                    if (value.isExpired()) {
                        cacheSize.decrementAndGet();
                        return null;
                    } else {

                        synchronized (evictionPolicy) {
                            evictionPolicy.onAccess(key);
                        }
                        return value;
                    }
                }
                return null;
            })).map(CacheValue::getValue);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    public void put(KEY key, VALUE value) {
        cacheLock.readLock().lock();
        try {
            cache.compute(key, (k, OldValue) -> {
                if (null == OldValue) {
                    if (cacheSize.get() == maxSize) {
                        synchronized (evictionPolicy) {
                            KEY removedKey = evictionPolicy.evict();
                            cache.remove(removedKey);
                        }
                        cacheSize.decrementAndGet();
                    }

                    cacheSize.incrementAndGet();

                    synchronized (evictionPolicy) {
                        evictionPolicy.onInsert(key);
                    }
                    return new CacheValue<>(value, ttlInMillis);

                }

                synchronized (evictionPolicy) {
                    evictionPolicy.onAccess(key);
                }
                return new CacheValue<>(value, ttlInMillis);
            });
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    private void startScheduledThread() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


        executorService.scheduleAtFixedRate(() -> {
            cacheLock.writeLock().lock();
            try {
                cache.forEach((key, value) -> {
                    if (value.isExpired()) {
                        evictionPolicy.evictKey(key);
                        cache.remove(key);
                        cacheSize.decrementAndGet();
                    }
                });
            } finally {
                cacheLock.writeLock().unlock();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}
