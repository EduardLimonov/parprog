package ru.spbstu.telematics.java;

import java.util.*;

class HashMap<K, V> {
    class Node {
        K key;
        V val;

        Node() {
        }

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
            if (n != null && ((Node) n).key_is(key))
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

    V put(K key, V val) {
        if (key == null || val == null)
            return null;

        if (!contains(key)) {
            addNode(new Node(key, val));
            return null;
        } else {
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
            if (((Node) nodes[i]).key_is(key)) {
                res = ((Node) nodes[i]).get_value();
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
                res.add(((Node) o).get_value());
        }
        return res;
    }

}
