package ru.spbstu.telematics.java;


import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class HashMap<K, V> {
    class Node {
        K key;
        V val;

        Node() {}

        Node(K key, V val) {
            this.key = key;
            this.val = val;
        }

        K get_key() {
            return key;
        }

        V get_value() {
            return val;
        }

        void setVal(V v) {
            val = v;
        }

        boolean is_equal(Node other) {
            return key.equals(other.get_key()) && val.equals(other.get_value());
        }

        boolean key_is(K key) {
            return Objects.equals(key, this.key);
        }

    }

    Object[] nodes;
    static final int DEFAULT_SIZE = 10, ADD_SIZE = 5;
    int last;

    HashMap() {
        last = -1;
        nodes = new Object[DEFAULT_SIZE];
    }

    HashMap(Collection<K> keys, Collection<V> vals) {
        Iterator<K> kIterator = keys.iterator();
        Iterator<V> vIterator = vals.iterator();

        last = -1;
        nodes = new Object[DEFAULT_SIZE];

        while (kIterator.hasNext()) {
            put(kIterator.next(), vIterator.next());
        }
    }

    boolean contains(K key) {
        for (int i = 0; i <= last; i++) {
            Object n = nodes[i];
            if (n != null && ((Node)n).key_is(key))
                return true;
        }
        return false;
    }

    int size() {
        return last + 1;
    }

    void prepareNodes() {
        if (last == nodes.length - 1)
            nodes = Arrays.copyOf(nodes, nodes.length + ADD_SIZE);
    }

    Node findNode(K key) {
        for (int i = 0; i <= last; i++) {
            Object n = nodes[i];
            if (((Node) n).key_is(key))
                return (Node) n;
        }
        return null;
    }

    V get(K key) {
        Node n = findNode(key);
        return n == null ? null : n.get_value();
    }

    void addNode(Node n) {
        prepareNodes();
        last++;
        nodes[last] = n;
    }

    V put (K key, V val) {
        if (key == null || val == null)
            return null;

        if (!contains(key)){
            addNode(new Node(key, val));
            return null;
        }
        else {
            Node n = findNode(key);
            V res = n.get_value();
            n.setVal(val);
            return res;
        }
    }

    void rmvNode(int i) {
        if (last - i >= 0) System.arraycopy(nodes, i + 1, nodes, i, last - i);
        nodes[last--] = null;
    }

    V remove(K key) {
        if (key == null)
            return null;
        V res = null;
        if (contains(key)) {
            res = findNode(key).get_value();
        }

        for (int i = 0; i <= last; i++)
            if (((Node)nodes[i]).key_is(key)) {
                res = ((Node)nodes[i]).get_value();
                rmvNode(i);
                break;
            }
        return res;
    }

    public Set<V> values() {
        Set<V> res = new HashSet<V>();
        for (int i = 0; i <= last; i++) {
            Object o = nodes[i];
            if (o != null)
                res.add(((Node)o).get_value());
        }
        return res;
    }

}

public class BidirectionalHashMap<K, V> implements BidiMap<K, V> { //Iterable<T> {
    HashMap<K, V> direct;
    HashMap<V, K> reverse;

    public BidirectionalHashMap() {
        direct = new HashMap<>();
        reverse = new HashMap<>();
    }

    BidirectionalHashMap(HashMap<K, V> dir, HashMap<V, K> rev) {
        direct = dir;
        reverse = rev;
    }

    BidirectionalHashMap(Collection<K> keys, Collection<V> vals) {
        direct = new HashMap<>(keys, vals);
        reverse = new HashMap<>(vals, keys);
    }

    @Override
    public V get(Object key) {
        return direct.get((K)key);
    }

    @Override
    public V put(K key, V value) {
        V v = null;
        if (direct.contains(key)) {
            v = direct.remove(key);
            reverse.remove(v);
        }
        else if (reverse.contains(value)) {
            K k = reverse.remove(value);
            direct.remove(k);
        }
        direct.put(key, value);
        reverse.put(value, key);

        return v;
    }

    @Override
    public V remove(Object key) {
        V res = direct.remove((K)key);
        reverse.remove(res);
        return res;
    }

    @Override
    public void putAll(Map<? extends K,? extends V> m) {
        for (K k: m.keySet()) {
            put(k, m.get(k));
        }
    }

    @Override
    public void clear() {
        direct = new HashMap<>();
        reverse = new HashMap<>();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(reverse.values());
    }

    public K getKey(Object value) {
        return reverse.get((V)value);
    }

    public BidirectionalHashMap<V,K> inverseBidiMap() {
        return new BidirectionalHashMap<V, K>(reverse, direct);
    }


    public K removeValue(Object value) {
        K k = reverse.remove((V)value);
        direct.remove(k);
        return k;
    }

    public Set<V> values() {
        return direct.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> res = new HashSet<>();
        for (K key: reverse.values()) {
            res.add(new Entry<K, V>() {

                @Override
                public K getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return direct.get(key);
                }

                @Override
                public V setValue(V value) {
                    V oldVal = direct.put(key, value);
                    reverse.remove(oldVal);
                    reverse.put(value, key);
                    return oldVal;
                }
            } );
        }
        return res;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if (get(key) != null)
            return get(key);
        return defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super K,? super V> action) {
        for (Entry<K, V> pair : entrySet()){
            action.accept(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K,? super V,? extends V> function) {
        for (Entry<K, V> pair: entrySet()) {
            V newValue = function.apply(pair.getKey(), pair.getValue());
            put(pair.getKey(), newValue);
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (get(key) == null) {
            put(key, value);
            return null;
        }
        return get(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (containsKey(key) && Objects.equals(get(key), value)) {
            remove(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (get(key) != null && Objects.equals(get(key), oldValue)) {
            put(key, newValue);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        if (get(key) != null)
            return put(key, value);
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K,? extends V> mappingFunction) {
        if (get(key) == null) {
            V val = mappingFunction.apply(key);
            if (val != null)
                put((K)key, val);
            return val;
        }
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K,? super V,? extends V> remappingFunction) {
        if (get(key) != null) {
            V newVal = remappingFunction.apply(key, get(key));
            if (newVal != null)
                put(key, newVal);
            return newVal;
        }
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K,? super V,? extends V> remappingFunction) {
        if (get(key) == null)
            return null;
        return remappingFunction.apply(key, get(key));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null)
            remove(key);
        else {
            put(key, newValue);
            return newValue;
        }
        return null;
    }

    public int size() {
        return direct.size();
    }

    @Override
    public boolean isEmpty() {
        return direct.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return direct.contains((K)key);
    }

    @Override
    public boolean containsValue(Object value) {
        return reverse.contains((V)value);
    }

    public Iterator<K> iterator() {
        return (Iterator<K>)mapIterator();
    }

    @Override
    public MapIterator<K, V> mapIterator() {
        return new BidiMapIterator<K, V>(this);
    }
}
