import java.util.HashMap;
import java.util.Map;

public class LRUEvictionPolicy<K> implements EvictionPolicy<K>{
    private Node<K> head;
    private Node<K> tail;
    private final Map<K, Node<K>> mp;

    private LRUEvictionPolicy() {
        head = null;
        tail = null;
        mp = new HashMap<>();
    }

    private static final class InstanceHolder {
        public static final LRUEvictionPolicy<?> INSTANCE = new LRUEvictionPolicy<>();
    }

    @SuppressWarnings("unchecked")
    public static <KEY> LRUEvictionPolicy<KEY> getInstance() {
        return (LRUEvictionPolicy<KEY>) InstanceHolder.INSTANCE;
    }

    @Override
    public void onInsert(K key) {
        Node<K> newNode = new Node<>(key);

        newNode.next = head;
        if (head != null) {
            head.prev = newNode;
        }
        head = newNode;

        if (tail == null) {
            tail = head;
        }

        mp.put(key, newNode);
    }

    @Override
    public void onAccess(K key) {
        Node<K> node = mp.get(key);

        if (null == node.prev) {
            return;
        }
        if (null == node.next) {

        }

    }

    @Override
    public K evict() {
        return null;
    }

    @Override
    public K evictKey(K key) {
        return null;
    }

    private static class Node<K> {
        Node<K> prev;
        Node<K> next;
        K key;

        public Node(K key) {
            this.key = key;
            this.prev = null;
            this.next = null;
        }
    }
}
