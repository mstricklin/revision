package edu.utexas.arlut.ciads.shadowMap;

import com.google.common.collect.FluentIterable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;

@Slf4j
public class ShadowMap<K extends Comparable<K>, V> implements Map<K, V> {
    // TODO:
    // toString()
    // test iterator
    // get history
    // tag
    // test size
    // values()
    // putAll size increment
    // test canonical short-cut

    public ShadowMap() {
        history.add(current);
    }
    public void commit() {
        current.locked = true;
        if (current.canonical) {
            // yes, use ==
            current.store.entrySet().removeIf(entry -> TOMBSTONE == entry.getValue());
        }
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
            if (r.store.containsValue(value)) {
                // make sure it hasn't been deleted later.
                K k = null;
                for (Map.Entry<K, V> e : r.store.entrySet()) {
                    if (e.getValue().equals(value))
                        k = e.getKey();
                }
                return containsKey(k);
            }
            if (r.canonical)
                break;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        V v = lookup(key);
        return tombstoneToNull(v);
    }

    @Override
    public V put(K key, V newV) {
        if (current.locked)
            rollForward();
        V oldV = tombstoneToNull(lookup(key));
        // put unconditionally?
        current.store.put(key, newV);
        if (null == oldV) {
            current.size++;
        }
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
//        current.store.putAll(m);
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
        // increment size...how?
    }

    @Override
    public void clear() {
        if (current.locked)
            rollForward();
        current.store.clear();
        current.canonical = true;
        current.size = 0;
    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks == null) {
            ks = new KeySet();
            this.keySet = (Set)ks;
        }
        return (Set)ks;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = this.entrySet;
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : es;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        checkNotNull(action);

        List<Revision<K, V>> revs = newArrayList();
//        history.stream().peek(r -> revs.add(r)).peek(r -> log.info("{}", r.store)).allMatch(r-> true);
        history.stream().peek(r -> revs.add(r)).peek(r -> log.info("{}", r.store)).allMatch(r -> !r.canonical);
        log.info("");
        history.stream().peek(r -> revs.add(r)).peek(r -> log.info("{}", r.store)).anyMatch(r -> r.canonical);

        log.info("");

        List<Revision<K, V>> l = newArrayList();
        for (Revision<K, V> r : history) {
            l.add(r);
            if (r.canonical) break;
        }
        Iterable<Entry<K, V>> it = FluentIterable.from(l)
                                                 .transformAndConcat(f -> f.store.entrySet());

        Iterator<Entry<K, V>> iter = it.iterator();
        while (iter.hasNext()) {
            Entry<K, V> e = iter.next();
            log.info("FI Entry {} => {}", e.getKey(), e.getValue());
        }

        log.info("");
        log.info("TakeUntil:");
        Iterable<Revision<K,V>> tu = new TakeUntil<>(history, IS_CANONICAL);
        for (Revision<K,V> r: tu)
            log.info("rev: {}", r);

//        Iterator<Entry<K,V>> it2 = TakeWhile(history.iterator(), r->r.canonical);
//        for (Entry e : it) {
//            log.info("FI Entry {} => {}", e.getKey(), e.getValue());
//        }
        for (Entry e : entrySet()) {
            action.accept((K)e.getKey(), (V)e.getValue());
        }
    }

    public void dump() {
        for (Revision r : history) {
            log.info("{}", r);
            if (r.canonical)
                break;
        }
    }
    public void dumpAll() {
        for (Revision r : history) {
            log.info("{}", r);
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Revision<K, V> r : history) {
            h = 31 * h + (r == null ? 0 : r.hashCode());
            if (r.canonical)
                break;
        }
        return h;
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
            if (r.canonical)
                break;
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
            return ShadowMap.this.size();
        }
        @Override
        public void clear() {
            ShadowMap.this.clear();
        }
        @Override
        public final boolean contains(Object o) {
            return ShadowMap.this.containsKey(o);
        }
        @Override
        public final boolean remove(Object key) {
            return null != ShadowMap.this.remove(key);
        }
        @Override
        public final void forEach(Consumer<? super K> action) {
            // TODO...
        }

    }

    final class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }
        @Override
        public int size() {
            return ShadowMap.this.size();
        }
        @Override
        public void clear() {
            ShadowMap.this.clear();
        }
        @Override
        public final boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry)o;
                return null != ShadowMap.this.remove(e.getKey());
            }
            return false;
        }
        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry)o;
            Object key = e.getKey();
            return ShadowMap.this.containsKey(key);
        }
    }

    // =================================
    final class EntryIterator extends SMIterator implements Iterator<Entry<K, V>> {
        EntryIterator() {
            super();
        }
        public final Entry<K, V> next() {
            return this.nextNode();
        }
    }

    final class KeyIterator extends SMIterator implements Iterator<K> {
        KeyIterator() {
            super();
        }
        public final K next() {
            return this.nextNode().getKey();
        }
    }

    final class ValueIterator extends SMIterator implements Iterator<V> {
        ValueIterator() {
            super();
        }
        public final V next() {
            return this.nextNode().getValue();
        }
    }

    // =================================
    private abstract class SMIterator {
        // TODO: concurrent modification exceptions?
        Entry<K, V> nextNode = null;
        Entry<K, V> currNode = null;
        Revision<K, V> rev = current;
        Iterator<Entry<K, V>> it = rev.store.entrySet().iterator();

        SMIterator() {
            List<Revision<K, V>> revs = newArrayList();
            for (Revision<K, V> r : history) {
                revs.add(r);
                if (r.canonical) break;
            }
//            FluentIterable.of(revs)
//                          .transformAndConcat(rev->rev.store.entrySet());
            history.stream().peek(rev -> revs.add(rev)).anyMatch(rev -> rev.canonical);

            if (it.hasNext())
                nextNode = it.next();

        }
        private Entry<K, V> next() {
            return it.hasNext() ? it.next() : null;
        }
        final Map.Entry<K, V> nextNode() {
            Entry n = nextNode;
            if (it.hasNext())
                nextNode = it.next();
            else {
                if (rev.canonical)
                    nextNode = null;
                else {
                    rev = rev.previous;
                    if (null != rev) {
                        it = rev.store.entrySet().iterator();
                        nextNode = next();
                    }
                }
            }
            return n;
        }
        public final boolean hasNext() {
            return nextNode != null;
        }
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // =================================
    private final Predicate<Revision<K,V>> IS_CANONICAL = r -> r.canonical;
    final class TakeUntil<T> implements Iterable<T> {
        List<T> l = newArrayList();
        public TakeUntil(Iterable<T> it, Predicate<T> pred) {
            for (T t: it) {
                l.add(t);
                if (pred.test(t))
                    break;
            }
        }
        @Override
        public Iterator<T> iterator() {
            return l.iterator();
        }
    }

    final class TakeWhile<T> implements Iterator<T> {
        Iterator<T> iter;
        Predicate<T> pred;
        volatile T next = null;
        public TakeWhile(Iterator<T> iter, Predicate<T> pred) {
            this.iter = iter;
            this.pred = pred;
            if (iter.hasNext()) {
                next = iter.next();
                if (!pred.test(next))
                    next = null;
            }
        }
        @Override
        public boolean hasNext() {
            return next != null;
        }
        @Override
        public T next() {
            if (iter.hasNext()) {
                next = iter.next();
                if (!pred.test(next))
                    next = null;
            }
            return next;
        }
    }

    // =======================================
    // don't want this interned, (we compare with ==, & don't want false
    //  positives) so put it on the heap.
    private static final Object TOMBSTONE = new String("TOMBSTONE");
    private static java.util.function.Predicate<Object> NOT_TOMBSTONE = v -> v != TOMBSTONE;


    private Revision<K, V> current = new Revision<>();

    // need to make this a singly-linked list, so it can be stitched into
    // other stacks
    private final Deque<Revision<K, V>> history = newArrayDeque();
    private transient Set<K> keySet;
    private transient Set<Entry<K, V>> entrySet;


    private static class Revision<K, V> {
        private Revision() { // the initial root
            locked = true;
            canonical = true;
        }
        private Revision(Revision<K, V> previous) {
            this.previous = previous;
            size = previous.size;
        }

        @Override
        public String toString() {
            return String.format("%d|%s %s %s", id, store.toString(), locked ? "locked" : "", canonical ? "canonical" : "");
        }

        private String tag = "";
        private boolean locked = false;
        private boolean canonical = false;
        private int size = 0;
        private Revision<K, V> previous = null;
        private Map<K, V> store = newHashMap();
        private static AtomicInteger ID = new AtomicInteger(0);
        private final int id = ID.getAndIncrement();
    }
}
