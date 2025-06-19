public interface EvictionPolicy<K> {
    void onInsert(K key);
    void onAccess(K key);
    K evict();
    K evictKey(K key);

}
