// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.chain;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.*;

public class ChainMap<K, V> extends AbstractMap<K, V> {
    public ChainMap() {
        history.addFirst(current);
    }
    public void commit() {
        current = new Revision<K, V>();
        history.addFirst(current);
    }
    public void rollback() {
        history.pop();
        current = new Revision<K, V>();
        history.addFirst(current);
    }
    @Override
    public Set<Entry<K, V>> entrySet() {
        // filter TOMBSTONE
        return current.store.entrySet();
    }
    @Override
    public V put(K k, V v) {
        return current.store.put(k, v);
    }
    @Override
    public V get(Object k) {
        for (Revision<K,V> r: history) {
            V v = r.store.get(k);
            if (TOMBSTONE.equals(v))
                return null;
            if (null != v)
                return v;
        }
        return null;
    }
    @Override
    public V remove(Object k) {
        if (containsKey(k)) {
            current.store.put((K)k, (V)TOMBSTONE);
        }
        return null;
    }

    private static final Object TOMBSTONE = new String("TOMBSTONE");
    private Revision<K, V> current = new Revision<>();
    private final Deque<Revision<K,V>> history = newLinkedList();


    private static class Revision<K,V> {
        private Revision() {
//            this.prior = prior;
        }
        public V get(Object k) {
            return store.get(k);
        }
        private final Map<K, V> store = newHashMap();
//        private final Revision prior;
    }
}
