public class CacheValue<V> {
    private final V value;
    private final long expiryTime;

    public CacheValue(V value, long ttlInMillis) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttlInMillis;
    }

    public V getValue() {
        return value;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() < expiryTime;
    }
}
