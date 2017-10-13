// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;

import java.util.*;

public class BMap<K extends Comparable<K>, V> implements Map<K, V> {
    private final int minWindow, maxWindow;
    private Node<K> root;
    BMap() {
        this.minWindow = 32;
        this.maxWindow = 2 * minWindow;
        Map<K, V> m = newHashMap();
        root = new Node<K>(maxWindow);
    }
    BMap(int minWindow) {
        this.minWindow = minWindow;
        this.maxWindow = 2 * minWindow;
        root = new Node<K>(maxWindow);
    }
    @Override
    public int size() {
        return 1;
    }
    @Override
    public boolean isEmpty() {
        return false;
    }
    @Override
    public boolean containsKey(Object o) {
        return false;
    }
    @Override
    public boolean containsValue(Object o) {
        return false;
    }
    @Override
    public V get(Object o) {
        return null;
    }
    @Override
    public V put(K k, V v) {
        if (null == root)
            root = new Node<K>(maxWindow);
        Node<K> home = findHome(root, k);
        home.put(k);
        return null;
    }
    private Node<K> findHome(Node<K> n, K k) {
        int i = Arrays.binarySearch(n.keys, 0, n.cnt, k);
        if (i >= 0)
            return n;
        return root;
    }
    // insert is always done on a leaf.
    // no need to keep track of alignment of children on a leaf.
    private void insert(K k) {
        Node<K> r = root;
        // root is full, split & push up
        if (r.full()) {
            root = new Node<K>(maxWindow);
            root.leaf = false;
            root.children[0] = r;
//            split(root, r);
        }
//        if (n.isLeaf()) {
//            if (n.cnt == maxWindow) {
//                // split & promote
//            } else {
//                // insert
//            }
//        }
    }
    @Override
    public V remove(Object o) {
        return null;
    }
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

    }
    @Override
    public void clear() {

    }
    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }
    @Override
    public Collection<V> values() {
        return Collections.emptyList();
    }
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }
    private static final class Node<K> {
        private Node(int window) {
            keys = (K[])(new Object[window]);
            children = new Node[window + 1];
        }
        private void put(K k) {
            keys[cnt] = k;
            cnt++;
        }
        private boolean isLeaf() {
            return true;
        }
        private boolean full() {
            return cnt == keys.length;
        }
        private final K[] keys;
        private final Node[] children;
        private int cnt = 0;
        private boolean leaf = true;
    }
}
