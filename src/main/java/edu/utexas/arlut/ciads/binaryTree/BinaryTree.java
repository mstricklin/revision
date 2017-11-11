// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.binaryTree;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BinaryTree<K extends Comparable<K>, V> {
    // what if K/V exist?
    public boolean add(K key, V value) {
        List<Node<K,V>> path = newArrayList();
        Node<K, V> newNode = new Node<>(key, value);
        if (null == root) {
            root = newNode;
            return true;
        }
        Node<K, V> node = clone(root);
        path.add(node);
        root = node;
        while (null != node) {
            if (newNode.compareTo(node) < 0) { // go left
                if (null == node.left) {
                    node.left = newNode;
                    break;
                }
                node.left = clone(node.left);

                node = node.left;
            } else { // go right
                if (null == node.right) {
                    node.right = newNode;
                    break;
                }
                node.right = clone(node.right);
                node = node.right;
            }
            path.add(node);
        }
        System.out.println(path.stream().map(Object::toString).collect(Collectors.joining(", ")));
        return true;
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
        return 0;
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

    private Node<K, V> root;
    private List<Node<K, V>> rootHistory = newArrayList();

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
            return "[" + id + (dirty ? "X|" : "|") + balance + "] " + key + " => " + value;
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

}
