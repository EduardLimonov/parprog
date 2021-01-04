package ru.spbstu.telematics.java;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Queue;

class BidiMapIterator<K, V> implements MapIterator<K, V> {
    final private Queue<K> keys;
    BidiMap<K, V> bidimap;
    K current;

    public BidiMapIterator(BidiMap<K, V> bmap) {
        bidimap = bmap;
        keys = new ArrayDeque<K>((Collection<? extends K>) bmap.values());
        current = null;
    }

    @Override
    public boolean hasNext() {
        return keys.size() > 0;
    }

    @Override
    public K next() throws NoSuchElementException {
        if (!hasNext())
            throw new NoSuchElementException();
        return current = keys.remove();
    }

    @Override
    public K getKey() throws IllegalStateException {
        if (current == null)
            throw new IllegalStateException();
        return current;
    }

    @Override
    public V getValue() throws IllegalStateException {
        if (current == null)
            throw new IllegalStateException();
        return bidimap.get(current);
    }

    @Override
    public void remove() throws IllegalStateException {
        if (current == null)
            throw new IllegalStateException();
        bidimap.remove(current);
        current = null;
    }

    @Override
    public V setValue(V v) {
        if (current == null)
            throw new IllegalStateException();
        return bidimap.put(current, v);
    }
}
