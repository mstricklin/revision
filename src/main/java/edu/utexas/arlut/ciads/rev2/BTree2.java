// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev2;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Joiner;
import com.google.common.collect.ObjectArrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BTree2<T extends Comparable<T>> {
    static Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
    private Node<T> root = null;
    private final int order;
    private int minKeySize = 1;
    private int minChildrenSize = minKeySize + 1; // 2
    private int maxKeySize = 2 * minKeySize; // 2
    private int maxChildrenSize = maxKeySize + 1; // 3
    // =================================
    public BTree2(int order) {
        this.order = order;
        minKeySize = order / 2;
        maxKeySize = (2*minKeySize);
        minChildrenSize = minKeySize + 1;
        maxChildrenSize = maxKeySize + 1;
        log.info("order {}", order);
        log.info("keySize {} {}", minKeySize, maxKeySize);
        log.info("childrenSize {} {}", minChildrenSize, maxChildrenSize);
    }
    // =================================
    public void put(T t) {
        if (null == root) {
            root = new Node(maxKeySize);
            root.add(t);
        } else {
            List<Node<T>> path = findHome(t);
            log.info("path: {}", COMMA_JOINER.join(path));
            // everything in path will be mutated...
            Node<T> l = getLast(path);
            if (l.isFull())
                split(path);

            Node<T> n = root;
            while (null != n) {
                if (n.isLeaf()) {
                    n.add(t);
                    if (n.isFull())
                        split(n);
                    break;
                }
                // else, find child...
                T least = n.values[0];
                if (t.compareTo(least) <= 0) {
                    n = n.children[0];
                    continue;
                }
                T greatest = n.values[n.valueCnt-1];
                if (t.compareTo(greatest) > 0) {
                    n = n.children[n.childCnt-1];
                    continue;
                }
                // else, somewhere in the middle...


            }
        }
    }
    private void split(List<Node<T>> path) {
        Node<T> last = getLast(path);
        Node<T> p = null;
        if (1 == path.size()) {
            doSplit(null, last);
        } else {
            doSplit(null, last);
        }

        int medianIdx = last.valueCnt / 2;
        T medianValue = last.values[medianIdx];

    }
    private void doSplit(Node<T> parent, Node<T> nodeToSplit) {

    }
    private List<Node<T>> findHome(T t) {
        if (null == root) {
            root = new Node(maxKeySize);
            return newArrayList(root);
        }
        Node<T> n = root;
        List<Node<T>> path = newArrayList();
        while (null != n) {
            path.add(n);
            if (n.isLeaf()) {
                return path;
            }
            // else, find child...
            T least = n.values[0];
            if (t.compareTo(least) <= 0) {
                n = n.children[0];
                continue;
            }
            T greatest = n.values[n.valueCnt-1];
            if (t.compareTo(greatest) > 0) {
                n = n.children[n.childCnt-1];
                continue;
            }
            // else, somewhere in the middle...
        }
        log.error("Didn't find path to {}...?", t);
        return Collections.emptyList();
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
    private void split(Node<T> nodeToSplit) {
//        log.info("Splitting {}", nodeToSplit);
        int medianIdx = nodeToSplit.valueCnt / 2;
        T medianValue = nodeToSplit.values[medianIdx];

        Node<T> parent = root;
        if (nodeToSplit == root) {
            root = new Node(maxKeySize);
            root.addChild(nodeToSplit);
            parent = root;
            // add children...
        }
        Node<T> newPeer = new Node(maxKeySize);
        // place on right side
        // TODO: clone...
        nodeToSplit.values[medianIdx] = null;
        for (int i = medianIdx + 1; i < nodeToSplit.valueCnt; i++) {
            newPeer.add(nodeToSplit.values[i]);
            nodeToSplit.values[i] = null;
        }
        nodeToSplit.valueCnt = medianIdx;
        parent.add(medianValue);
        parent.addChild(newPeer);
//        log.info("old node: {}", nodeToSplit);
//        log.info("new peer: {}", newPeer);
    }
    // =================================
    private static class Node<T extends Comparable<T>> {
        private Node(int order) {
            // +1, to hold while splitting and re-organizing
            values = (T[])new Comparable[order + 1];
            this.splay = order;
        }
        private Comparator<Node<T>> comparator = new Comparator<Node<T>>() {
            @Override
            public int compare(Node<T> a, Node<T> b) {
                return a.values[0].compareTo(b.values[0]);
            }
        };
        private boolean isLeaf() {
            return 0 == childCnt;
        }
        private boolean isFull() {
            return splay < valueCnt;
        }
        private int indexOf(T t) {
            return Arrays.binarySearch(values, 0, valueCnt, t);
        }
        private void add(T t) {
            values[valueCnt++] = t;
            Arrays.sort(values, 0, valueCnt);
        }
        private Node<T> split(Node<T> parent) {
            Node<T> newNode = new Node<>(splay);
            if (null == parent) { // I'm the root
                Node<T> newRoot = new Node(splay);
                parent = newRoot;
                Node<T> peer = new Node(splay);

            }
            return newNode;
        }
        private void addChild(Node<T> child) {
            allocateChildren();
            children[childCnt++] = child;
            Arrays.sort(children, 0, childCnt, comparator);
        }

        private void allocateChildren() {
            if (null == children)
                children = new Node[splay+2];
        }
        private static <T extends Comparable<T>> Node<T> takeHalf(Node<T> node) {
            int medianIdx = node.childCnt / 2;
            Node<T> newPeer = new Node<T>(node.splay);
            newPeer.values = Arrays.copyOfRange(node.values, medianIdx + 1, node.valueCnt);
            if (null != node.children)
                newPeer.children = Arrays.copyOfRange(node.children, medianIdx + 1, node.childCnt);
            return newPeer;
        }
        private Node<T> leftSibling() {
//            if (null == parent) return null;
//            int myIndex = Arrays.binarySearch(parent.children, this);
//            if (0 == myIndex) // I'm the first
//                return null;
//            return parent.children[myIndex - 1];
            return null;
        }
        private Node<T> rightSibling() {
//            if (null == parent) return null;
//            int myIndex = Arrays.binarySearch(parent.children, this);
//            if (parent.childCnt >= myIndex + 1) // I'm the last
//                return null;
//            return parent.children[myIndex + 1];
            return null;
        }
        @Override
        public String toString() {
            return "["+id+"] "+COMMA_JOINER.join(values);
        }
        // =================================
        // splay is always an even number. When the number of values reaches splay+1 (an odd number),
        // we split
        private static AtomicInteger ID = new AtomicInteger(0);
        private final int id = ID.getAndIncrement();
        private Node<T>[] children = null;
        private int childCnt = 0;
        private T[] values = null;
        private int valueCnt = 0;
        private final int splay;

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
            builder.append(node.toString()).append("\n");
//            COMMA_JOINER.appendTo(builder, node.values);

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

}
