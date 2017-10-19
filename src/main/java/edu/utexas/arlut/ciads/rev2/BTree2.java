// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev2;

import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.collect.ObjectArrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BTree2<T extends Comparable<T>> {
    static Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
    public BTree2(int order) {
        this.order = order;
    }
    public void add(T t) {
        // split preëmptively on the way down?
//        Node<T> n = findHome(t);
//        n.add(t);
        if (null == root) {
            root = new Node(null, order);
            root.add(t);
        } else {
            Node<T> n = root;
            while (null != n) {
                if (n.isLeaf()) {
                    n.add(t);
//                    if (n.isFull())
//                        split(n);
//                    else
//                        break;
                }
            }
        }
    }
    private Node<T> findHome(T t) {
        if (null == root) {
            root = new Node(null, order);
            return root;
        }
        if (root.valueCnt < order)
            return root;
        return null;
    }
    private Node<T> container(T t) {
        Node<T> n = root;
        do {
            if (0 >= n.indexOf(t))
                return n;

        } while (!n.isLeaf());
        return null;
    }
    private boolean contains(T t) {
        Node<T> n = root;
        do {
            if (0 >= n.indexOf(t))
                return true;

        } while (n != null && !n.isLeaf());
        return false;
    }
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }
    private void split(Node<T> node) {
        log.info("Splitting {}", node);
        int medianIdx = node.childCnt / 2;

        Node<T> parent = node.parent;
        if (node == root) {
            Node<T> newRoot = new Node(null, order);
            parent = newRoot;
            node.parent = newRoot;
        }
        Node<T> newPeer = new Node(parent, order);
        // place on right side
        newPeer.takeHalf(node);
    }
    // =================================
    private static class Node<T extends Comparable<T>> {
        private Node(Node<T> parent, int order) {
            this.parent = parent;
            // +1, to hold while splitting and re-organizing
            values = (T[])new Comparable[order + 1];
            this.order = order;
        }
        private boolean isLeaf() {
            return 0 == childCnt;
        }
        private int indexOf(T t) {
            return Arrays.binarySearch(values, 0, valueCnt, t);
        }
        private void add(T t) {
            ObjectArrays.concat(values, t);
            values[valueCnt++] = t;
            Arrays.sort(values, 0, valueCnt);
        }
        private void split(Node<T> parent) {
            if (null == parent) { // I'm the root
                Node<T> newRoot = new Node(null, order);
                parent = newRoot;
                Node<T> peer = new Node(newRoot, order);

            }

        }
        private static <T extends Comparable<T>> Node<T> takeHalf(Node<T> node) {
            int medianIdx = node.childCnt / 2;
            Node<T> newPeer = new Node<T>(node.parent, node.order);
            newPeer.values = Arrays.copyOfRange(node.values, medianIdx + 1, node.valueCnt);
            if (null != node.children)
                newPeer.children = Arrays.copyOfRange(node.children, medianIdx + 1, node.childCnt);
            return newPeer;
        }
        private Node<T> leftSibling() {
            if (null == parent) return null;
            int myIndex = Arrays.binarySearch(parent.children, this);
            if (0 == myIndex) // I'm the first
                return null;
            return parent.children[myIndex - 1];
        }
        private Node<T> rightSibling() {
            if (null == parent) return null;
            int myIndex = Arrays.binarySearch(parent.children, this);
            if (parent.childCnt >= myIndex + 1) // I'm the last
                return null;
            return parent.children[myIndex + 1];
        }
        // =================================
        private static int ID = 0;
        private final int id = ID++;
        private Node<T> parent;
        private Node[] children = null;
        private int childCnt = 0;
        private T[] values = null;
        private int valueCnt = 0;
        private final int order;

    }

    // =================================
    private static class TreePrinter {

        public static <T extends Comparable<T>> String getString(BTree2<T> tree) {
            if (tree.root == null) return "Tree has no nodes.";
            return getString(tree.root, "", true);
        }

        private static <T extends Comparable<T>> String getString(Node<T> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix).append((isTail ? "└── " : "├── "));
            COMMA_JOINER.appendTo(builder, node.values);

//            builder.append(" ("+node.id+(node.dirty?"X":"")+")\n");

            if (node.children != null) {
                for (int i = 0; i < node.childCnt - 1; i++) {
                    Node<T> child = node.children[i];
                    builder.append(getString(child, prefix + (isTail ? "    " : "│   "), false));
                }
                if (node.childCnt >= 1) {
                    Node<T> obj = node.children[node.childCnt - 1];
                    builder.append(getString(obj, prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }

    // =================================
    private Node<T> root = null;
    private final int order;
}
