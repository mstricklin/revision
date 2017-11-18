// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev2;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.jwetherell.algorithms.data_structures.BTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BTree3<T extends Comparable<T>> {
    static Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
    private Node<T> root = null;
    private final int splay;
    private int minValue = 1;
    private int minChildrenSize = minValue + 1; // 2
    private int maxValueSize = 2 * minValue; // 2
    private int maxChildrenSize = maxValueSize + 1; // 3
    private int size = 0;
    // =================================
    public BTree3() {
        // a 2-3 tree
        splay = 2;
    }
    public BTree3(int splay) {
        this.splay = splay;
        minValue = splay / 2;
        maxValueSize = (2 * minValue);
        minChildrenSize = minValue + 1;
        maxChildrenSize = maxValueSize + 1;
        log.info("splay {}", splay);
        log.info("keySize {} {}", minValue, maxValueSize);
        log.info("childrenSize {} {}", minChildrenSize, maxChildrenSize);
    }
    // =================================
    public void put(T t) {

        if (null == root) {
            root = new Node(maxValueSize);
            root.add(t);
            return;
        }
        List<Node<T>> path = pathForUpdate(t);
        log.info("insert {} path: {}", t, path);

        System.out.println(toString());
        Node<T> home = getLast(path);
        home.add(t);

        PeekingIterator<Node<T>> it = Iterators.peekingIterator(Lists.reverse(path).iterator());
        while (it.hasNext()) {
            Node<T> n = it.next();
            Node<T> parent = it.hasNext() ? it.peek() : null;
            log.info("path element {} parent {}", n, parent);
            if (n.isFull())
                split(parent, n);
            else
                break;
        }

//        last = it.next();
//        for (Node<T> n: Lists.reverse(path)) {
//            if (n.isFull())
//                split(n);
//            else
//                break;
//        }

//        Node<T> parent = null;
//        Node<T> node = root;
//        while (true) {
//            if (node.isFull()) {
//                split(parent, node);
//                node = root;
//                parent = null;
//                continue;
//            }
//            if (node.isLeaf())
//                break;
//
//            parent = node;
//
////            if (node.contains(t)) {
////                break;
////            }
//
//            T least = node.values[0];
//            if (t.compareTo(least) < 0) {
//                node = node.children[0];
//                continue;
//            }
//            T greatest = node.values[node.valueCnt - 1];
//            if (t.compareTo(greatest) > 0) {
//                node = node.children[node.childCnt - 1];
//                continue;
//            }
//            // else, somewhere in the middle...
//            for (int i = 1; i < node.childCnt; i++) {
//                T prev = node.getValue(i - 1);
//                T next = node.getValue(i);
//                if (t.compareTo(prev) > 0 && t.compareTo(next) <= 0) {
//                    log.info("descend point {}", i);
//                    node = node.getChild(i);
//                    break;
//                }
//            }
//
//        }
        System.out.println(toString());
//        node.add(t);
    }
    // =================================
    private List<Node<T>> pathForUpdate(T t) {
//        get the path to insert an item
        // must TODO: mutable nodes
        List<Node<T>> path = new ArrayList<>(8);
        if (null == root) {
            root = new Node(maxValueSize);
            path.add(root);
            return path;
        }
        Node<T> node = root;
        while (null != node) {
            // node = mutable node
            path.add(node);
            if (node.isLeaf()) {
                break;
            }
            // Lesser or equal
            T least = node.getValue(0);
            if (t.compareTo(least) < 0) {
                node = node.getChild(0);
                continue;
            }

            // Greater
            T greatest = node.getValue(node.valueCnt - 1);
            if (t.compareTo(greatest) > 0) {
                node = node.getChild(node.childCnt - 1);
                continue;
            }

            // Search internal nodes
            // TODO: try binarySearch...
            for (int i = 1; i < node.valueCnt; i++) {
                T prev = node.getValue(i - 1);
                T next = node.getValue(i);
                if (t.compareTo(prev) > 0 && t.compareTo(next) <= 0) {
                    node = node.getChild(i);
                    break;
                }
            }
        }
        return path;
    }
    // =================================
    private final int avgSplay = (maxChildrenSize + minChildrenSize) / 2;
    private int depth() {
        // approximate value
        // log (base splay)(size)
        //  Math.log(size) / Math.log(splay);
        return 1 + (int)(Math.log(size) / Math.log(avgSplay));
    }
    private void split(Node<T> parent, Node<T> nodeToSplit) {
        int medianIdx = nodeToSplit.valueCnt / 2;
        T medianValue = nodeToSplit.getValue(medianIdx);

        Node<T> left = new Node<T>(maxValueSize);
        for (int i = 0; i < medianIdx; i++) {
            left.add(nodeToSplit.getValue(i));
        }
        Node<T> right = new Node<T>(maxValueSize);
        for (int i = medianIdx + 1; i < nodeToSplit.valueCnt; i++) {
            right.add(nodeToSplit.getValue(i));
        }

        if (nodeToSplit.childCnt > 0) {
            for (int j = 0; j <= medianIdx; j++) {
                Node<T> c = nodeToSplit.getChild(j);
                left.addChild(c);
            }
            for (int j = medianIdx + 1; j < nodeToSplit.childCnt; j++) {
                Node<T> c = nodeToSplit.getChild(j);
                right.addChild(c);
            }
        }

        if (parent == null) {
            // new root, height of tree is increased
            root = parent = new Node<T>(maxValueSize);
            parent.add(medianValue);
            parent.addChild(left);
            parent.addChild(right);
        } else {
            // Move the median value up to the parent
            parent.add(medianValue);
            parent.removeChild(nodeToSplit);
            parent.addChild(left);
            parent.addChild(right);
        }
    }
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }
    // =================================

    private static class Node<T extends Comparable<T>> {
        private Node(int order) {
            values = (T[])new Comparable[order+1];
            this.splay = order;
        }
        private boolean isFull() {
            return valueCnt > splay;
        }
        private boolean isLeaf() {
            return 0 == childCnt;
        }
        private void add(T t) {
            values[valueCnt++] = t;
            Arrays.sort(values, 0, valueCnt);
        }
        T getValue(int i) {
            return values[i];
        }
        Node<T> getChild(int i) {
            return children == null ? null : children[i];
        }
        private void allocateChildren() {
            if (null == children)
                children = new Node[splay + 2];
        }
        private void addChild(Node<T> child) {
            allocateChildren();
            children[childCnt++] = child;
            Arrays.sort(children, 0, childCnt, COMPARATOR2);
        }
        private boolean removeChild(Node<T> child) {
            boolean found = false;
            if (childCnt == 0)
                return found;
            for (int i = 0; i < childCnt; i++) {
                if (children[i].equals(child)) {
                    found = true;
                } else if (found) {
                    // shift the rest of the keys down
                    children[i - 1] = children[i];
                }
            }
            if (found) {
                childCnt--;
                children[childCnt] = null;
            }
            return found;
        }
        private Node<T> split() {
            Node<T> newPeer = new Node<>(splay);
            int medianIdx = valueCnt / 2;
            T medianValue = values[medianIdx];
            values[medianIdx] = null;
            for (int i = medianIdx + 1; i < valueCnt; i++) {
                newPeer.add(values[i]);
                values[i] = null;
            }
            for (int i = medianIdx + 1; i < childCnt; i++) {
                newPeer.addChild(getChild(i));
                children[i] = null;
            }
            valueCnt = medianIdx;
            childCnt = medianIdx + 1;

            return newPeer;
        }
        @Override
        public String toString() {
            return "[" + id + "] " + COMMA_JOINER.join(values);
        }
        // =================================
        private static AtomicInteger ID = new AtomicInteger(0);
        private final int id = ID.getAndIncrement();
        private Node<T>[] children = null;
        private int childCnt = 0;
        private T[] values = null;
        private int valueCnt = 0;
        private final int splay;
        private Comparator<Node<T>> COMPARATOR = new Comparator<Node<T>>() {
            @Override
            public int compare(Node<T> a, Node<T> b) {
                return a.values[0].compareTo(b.values[0]);
            }
        };
        private Comparator<Node<T>> COMPARATOR2 = (a, b) ->
                a.values[0].compareTo(b.values[0]);
    }

    // =================================
    private static class TreePrinter {

        public static <T extends Comparable<T>> String getString(BTree3<T> tree) {
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

}
