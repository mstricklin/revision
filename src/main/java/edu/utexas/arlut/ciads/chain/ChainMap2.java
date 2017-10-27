package edu.utexas.arlut.ciads.chain;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;

@Slf4j
public class ChainMap2<K extends Comparable<K>, V> implements Map<K, V> {
    public ChainMap2() {
        history.add(current);
    }
    public void commit() {
        current.locked = true;
    }
    public void rollback() {
        history.pop();
        current = history.peek();
    }
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        V v = lookup(key);
        return (null != v && TOMBSTONE != v);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Revision<K,V> r : history) {
            log.info("{}", r.store);
            for (Map.Entry<K,V> e: r.store.entrySet()) {
                if (e.getValue().equals(value)) {
                    V v = lookup(e.getKey());
                    if (null != v && TOMBSTONE != v) {
                        log.info("found: {} {}", e, v);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        V v = lookup(key);
        return tombstoneToNull(v);
    }

    @Override
    public V put(K key, V value) {
        if (current.locked)
            rollForward();
        V oldV = lookup(key);
        current.store.put(key, value);
        return tombstoneToNull(oldV);
    }

    @Override
    public V remove(Object key) {
        if (current.locked)
            rollForward();
        V oldV = lookup(key);
        current.store.put((K) key, (V) TOMBSTONE);
        return tombstoneToNull(oldV);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (current.locked)
            rollForward();
        current.store.putAll(m);
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

    }

    public void dump() {
        for (Revision r : history) {
            log.info("{} {}", r.store, r.locked);
        }
    }

    // =======================================
    private V lookup(Object k) {
        for (Revision<K,V> r : history) {
            V v = r.store.get(k);
            if (null != v)
                return v;
        }
        return null;
    }

    private void rollForward() {
        current.locked = true;
        current = new Revision<>(false);
        history.push(current);
    }

    private <V> V tombstoneToNull(V v) {
        return (TOMBSTONE == v ? null : v);
    }

    // =======================================
    // don't want this interned, (we compare with ==, & don't want false
    //  positives) so put it on the heap.
    private static final Object TOMBSTONE = new String("TOMBSTONE");

    private Revision<K, V> current = new Revision<>(true);

    // need to make this a singly-linked list, so it can be stitched into
    // other stacks
    private final Deque<Revision<K,V>> history = newArrayDeque();


    private static class Revision<K, V> {
        private Revision(boolean locked) {
            this.locked = locked;
        }

        private String tag = "";
        private boolean locked = true;
        private Map<K, V> store = newHashMap();
    }
}
