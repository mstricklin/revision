package edu.utexas.arlut.ciads.chain;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;

import javax.annotation.Nullable;

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
        return current.size;
    }

    @Override
    public boolean isEmpty() {
        return 0 == current.size;
    }

    @Override
    public boolean containsKey(Object key) {
        V v = lookup(key);
        return (null != v && TOMBSTONE != v);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Revision<K, V> r : history) {
            log.info("{}", r.store);
            for (Map.Entry<K, V> e : r.store.entrySet()) {
                if (e.getValue().equals(value)) {
                    V v = lookup(e.getKey());
                    if (null != v && TOMBSTONE != v) {
//                        log.info("found: {} {}", e, v);
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
        V oldV = tombstoneToNull(lookup(key));
        current.store.put(key, value);
        if (null == oldV)
            current.size++;
        return oldV;
    }

    @Override
    public V remove(Object key) {
        V oldV = tombstoneToNull(lookup(key));
        if (null == oldV)
            return null;
        if (current.locked)
            rollForward();
        current.store.put((K)key, (V)TOMBSTONE);
        current.size--;
        return oldV;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (current.locked)
            rollForward();
        current.store.putAll(m);
    }

    @Override
    public void clear() {
        if (current.locked)
            rollForward();
        // TODO: get keySet, then mark all as TOMBSTONE
        current.size = 0;
    }

    private boolean found(final Deque<Revision<K, V>> h, K k) {
        return false;
    }
    public void allKeys() {
        final Deque<Revision<K, V>> h = newArrayDeque();
        log.info("allKeys");
//        dump();
        for (Revision<K, V> r : history) {
            log.info("{} {}", r.store, r.locked);
            for (Entry<K, V> e : r.store.entrySet()) {
                log.info("{}", e);
            }
        }
        log.info("");
        for (Revision<K, V> r : history) {
            log.info("{} {}", r.store, r.locked);
            nextEntry:
            for (Entry<K, V> e : Maps.filterValues(r.store, v -> v!=TOMBSTONE).entrySet()) {

                Iterables.any(h, (Revision<K,V> rev) -> r.store.containsKey(e.getKey()));
                for (Revision<K, V> r0 : h) {
                    log.info("\t{} testing against {}", e.getKey(), r0.store);
                    if (r0.store.containsKey(e.getKey())) {
                        log.info("\t\t!!!Found");
                        continue nextEntry;
                    }
                }
                log.info("{}", e);
            }
            h.push(r);

//            for (Entry<K,V> e: r.store.entrySet()) {
//                log.info("{}", e);
//            }
        }
    }

    @Override
    public Set<K> keySet() {
        return null;
//        Set<K> ks = this.keySet;
//        if (ks == null) {
//            ks = new KeySet();
//            this.keySet = (Set)ks;
//        }
//
//        return (Set)ks;
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

    @Override
    public String toString() {
        return "ZZZ";
    }

    // =======================================
    private V lookup(Object k) {
        for (Revision<K, V> r : history) {
            V v = r.store.get(k);
            if (null != v)
                return v;
        }
        return null;
    }

    private void rollForward() {
        current.locked = true;
        current = new Revision<>(current);
        history.push(current);
    }

    private <V> V tombstoneToNull(V v) {
        return (TOMBSTONE == v ? null : v);
    }
    // =================================
    final class KeySet extends AbstractSet<K> {


        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        @Override
        public int size() {
            return ChainMap2.this.size();
        }
        @Override
        public void clear() {
            ChainMap2.this.clear();
        }
        public final boolean contains(Object o) {
            return ChainMap2.this.containsKey(o);
        }
        public final boolean remove(Object key) {
            return null != ChainMap2.this.remove(key);
        }
        public final void forEach(Consumer<? super K> action) {

        }

    }

    static class Node<K, V> implements Entry<K, V> {
        final K key;
        V value;
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public final K getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.value;
        }
        public final V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }
        @Override
        public final String toString() {
            return this.key + "=" + this.value;
        }
        @Override
        public final int hashCode() {
            return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
        }


    }

    // =================================
    final class EntryIterator extends ChainMap2<K, V>.ChainMapIterator implements Iterator<Entry<K, V>> {
        EntryIterator() {
            super();
        }
        public final Entry<K, V> next() {
            return this.nextNode();
        }
    }

    final class KeyIterator extends ChainMap2<K, V>.ChainMapIterator implements Iterator<K> {
        KeyIterator() {
            super();
        }
        public final K next() {
            return this.nextNode().key;
        }
    }

    final class ValueIterator extends ChainMap2<K, V>.ChainMapIterator implements Iterator<V> {
        ValueIterator() {
            super();
        }
        public final V next() {
            return this.nextNode().value;
        }
    }

    // =================================
    abstract class ChainMapIterator {
        ChainMapIterator() {

        }
        final ChainMap2.Node<K, V> nextNode() {
            return null;
        }
        public final boolean hasNext() {
            return false;
//            return this.next != null;
        }
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // =======================================
    // don't want this interned, (we compare with ==, & don't want false
    //  positives) so put it on the heap.
    private static final Object TOMBSTONE = new String("TOMBSTONE");
    private static java.util.function.Predicate<Object> NOT_TOMBSTONE2 = v -> v != TOMBSTONE;


    private Revision<K, V> current = new Revision<>();

    // need to make this a singly-linked list, so it can be stitched into
    // other stacks
    private final Deque<Revision<K, V>> history = newArrayDeque();


    private static class Revision<K, V> {
        private Revision() {
            locked = true;
        }
        private Revision(Revision<K, V> parent) {
            this.parent = parent;
            size = parent.size;
        }

        private String tag = "";
        private boolean locked = false;
        private int size = 0;
        private Revision<K, V> parent = null;
        private Map<K, V> store = newHashMap();
    }
}
