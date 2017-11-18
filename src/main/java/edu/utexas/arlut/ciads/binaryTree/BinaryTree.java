// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.binaryTree;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Lists.reverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryTree<K extends Comparable<K>, V> {
    // what if K/V exist?
    public boolean add(K key, V value) {
        List<Node<K,V>> ppp = doAdd(key, value);
        for (Node<K,V> n: reverse(ppp)) {
            log.info("\titem: {}", n);
            int lHeight = null == n.left ? 0 : n.left.balance;
            int rHeight = null == n.right ? 0 : n.right.balance;
            log.info("\t\tL{} R{} {}", lHeight, rHeight, n.balance);
            if (lHeight>rHeight)
                n.balance = lHeight+1;
            else if (lHeight<rHeight)
                n.balance = rHeight+1;
            else
                n.balance = 0;
        }
        System.out.println(ppp.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return true;


//        List<Node<K,V>> path = newArrayList();
////        Node<K, V> newNode = new Node<>(key, value);
//        if (null == root) {
//            root = new Node<>(key, value);
//            size++;
//            return true;
//        }
//        Node<K, V> node = clone(root);
//        path.put(node);
//        root = node;
//        while (null != node) {
//            if (0 > node.key.compareTo(key)) { // go left, young man
//                if (null == node.left) {
//                    node.left = new Node<>(key, value);
//                    size++;
//                    break;
//                }
//                node.left = clone(node.left);
//                node = node.left;
//            } else if (0 < node.key.compareTo(key)) { // go right
//                if (null == node.right) {
//                    node.right = new Node<>(key, value);
//                    size++;
//                    break;
//                }
//                node.right = clone(node.right);
//                node = node.right;
//            } else if (0 == node.key.compareTo(key)) {
//                node.value = value;
//                break;
//            }
//            path.put(node);
//        }
//        System.out.println(path.stream().map(Object::toString).collect(Collectors.joining(", ")));
//        return true;
    }
    private List<Node<K,V>> doAdd(K key, V value) {
        if (null == root) {
            root = new Node<>(key, value);
            return Collections.emptyList();
        }
        List<Node<K,V>> path = new ArrayList<>(log2nlz(size) + 1);
        Node<K, V> node = clone(root);
        path.add(node);
        root = node;
        while (null != node) {
            if (0 > node.key.compareTo(key)) { // go left, young man
                if (null == node.left) {
                    node.left = new Node<>(key, value);
                    return path;
                }
                node.left = clone(node.left);
                node = node.left;
            } else if (0 < node.key.compareTo(key)) { // go right
                if (null == node.right) {
                    node.right = new Node<>(key, value);
                    return path;
                }
                node.right = clone(node.right);
                node = node.right;
            } else if (0 == node.key.compareTo(key)) {
                node.value = value;
                return path;
            }
            path.add(node);
        }
//        System.out.println(path.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return Collections.emptyList();
    }
    public void commit() {
        commit(root);
        rootHistory.add(root);
    }
    private void commit(Node<K, V> n) {
        if (null == n)
            return;
        if (n.dirty) {
            n.dirty = false;
            commit(n.left);
            commit(n.right);
        }
    }
    public V remove(K key) {
        return null;
    }
    public boolean contains(K key) {
        return null != find(key);
    }
    public V get(K key) {
        Node<K, V> n = find(key);
        return n == null ? null : n.value;
    }
    public int size() {
        return size;
    }
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }
    public void allTime() {
        for (Node<K, V> r : rootHistory) {
            System.out.println(TreePrinter.getString(r, ""));
        }
    }
    // =================================
    private Node<K, V> clone(Node<K, V> n) {
        return n.dirty ? n : new Node(n);
    }

    // =================================
    private Node<K, V> find(K key) {
        log.error("this find impl is wrong...");
        Node<K, V> node = root;
        while (null != node) {
            if (node.key.equals(key))
                return node;
            if (node.compareTo(node) < 0) { // go left
                node = node.left;
            } else { // go right
                node = node.right;
            }
        }
        return null;
    }
    private Node<K, V> findForInsert(K key) {
        if (null == root)
            return null;
        Node<K, V> node = clone(root);
        root = node;
        while (null != node) {
            if (node.key.equals(key))
                return node;
            if (node.compareTo(node) < 0) { // go left
                if (null == node.left)
                    return node;
                node.left = clone(node.left);
                node = node.left;
            } else { // go right
                if (null == node.right)
                    return node;
                node.right = clone(node.right);
                node = node.right;
            }
        }
        return null;
    }
    // =================================
    private Node<K, V> root;
    private List<Node<K, V>> rootHistory = newArrayList();
    private int size = 0;
    // =================================
    private static class Node<K extends Comparable<K>, V> implements Comparable<Node<K, V>> {
        K key;
        V value;
        boolean dirty = true;
        Node<K, V> left, right;
        private static AtomicInteger CNT = new AtomicInteger(0);
        private int id = CNT.getAndIncrement();
        private int balance = 0;

        Node(K key, V value) {
            checkNotNull(key);
            this.key = key;
            this.value = value;
        }
        Node(Node<K, V> node) {
            this.key = node.key;
            this.value = node.value;
            this.left = node.left;
            this.right = node.right;
            this.balance = node.balance;
        }
        @Override
        public int compareTo(Node<K, V> that) {
            return key.compareTo(that.key);
        }
        @Override
        public String toString() {
            return String.format("[%02d|%s|%d] %s=>%s", id, (dirty?"X":" "), balance, key, value);
        }
    }

    // =================================
    private static class TreePrinter {
        public static <K extends Comparable<K>, V> String getString(BinaryTree<K, V> tree) {
            if (tree.root == null)
                return "Tree has no nodes.";
            return getString(tree.root, "");
        }
        private static <K extends Comparable<K>, V> String getString(BinaryTree.Node<K, V> node, String prefix) {
            StringBuilder builder = new StringBuilder().append(prefix).append(node).append('\n');
            if (null != node.left)
                builder.append(getString(node.left, prefix + "L\t"));
            if (null != node.right)
                builder.append(getString(node.right, prefix + "R\t"));
            return builder.toString();
        }
    }
    // =================================
    private static int log2nlz( int bits ) {
        if( bits == 0 )
            return 0;
        return 31 - Integer.numberOfLeadingZeros( bits );
    }
}
