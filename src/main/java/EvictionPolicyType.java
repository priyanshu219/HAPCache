public enum EvictionPolicyType {
    LFU {
        @Override
        <K> EvictionPolicy<K> evictionPolicy() {
            return LRUEvictionPolicy.getInstance();
        }
    },
    LRU {
        @Override
        <K> EvictionPolicy<K> evictionPolicy() {
            return null;
        }
    };



    abstract <K> EvictionPolicy<K> evictionPolicy();
}