// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev;

import java.util.*;

/** Modified from java-algorithms-implementation
 *  at https://code.google.com/archive/p/java-algorithms-implementation/
 *  released under Apache license 2.0
 */

/**
 * B-tree is a tree data structure that keeps data sorted and allows searches,
 * sequential access, insertions, and deletions in logarithmic time. The B-tree
 * is a generalization of a binary search tree in that a node can have more than
 * two children. Unlike self-balancing binary search trees, the B-tree is
 * optimized for systems that read and write large blocks of data. It is
 * commonly used in databases and file-systems.
 * <p>
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 * @see <a href="https://en.wikipedia.org/wiki/B-tree">B-Tree (Wikipedia)</a>
 * <br>
 */
@SuppressWarnings("unchecked")
public class BTree<K extends Comparable<K>, V> implements ITree<K, V> {

    // Default to 2-3 Tree
    private int minKeySize = 1;
    private int minChildrenSize = minKeySize + 1; // 2
    private int maxKeySize = 2 * minKeySize; // 2
    private int maxChildrenSize = maxKeySize + 1; // 3

    private Node<K, V> root = null;
    private int size = 0;

    /**
     * Constructor for B-Tree which defaults to a 2-3 B-Tree.
     */
    public BTree() {
    }

    /**
     * Constructor for B-Tree of ordered parameter. Order here means minimum
     * number of keys in a non-root node.
     *
     * @param order of the B-Tree.
     */
    public BTree(int order) {
        this.minKeySize = order;
        this.minChildrenSize = minKeySize + 1;
        this.maxKeySize = 2 * minKeySize;
        this.maxChildrenSize = maxKeySize + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(K key, V value) {
        if (root == null) {
            root = new Node<K, V>(null, maxKeySize, maxChildrenSize);
            root.addKey(key);
        } else {
            Node<K, V> node = root;
            while (node != null) {
                if (node.numberOfChildren() == 0) {
                    node.addKey(key);
                    if (node.numberOfKeys() <= maxKeySize) {
                        // A-OK
                        break;
                    }
                    // Need to split up
                    split(node);
                    break;
                }
                // Navigate

                // Lesser or equal
                K lesser = node.getKey(0);
                if (key.compareTo(lesser) <= 0) {
                    node = node.getChild(0);
                    continue;
                }

                // Greater
                int numberOfKeys = node.numberOfKeys();
                int last = numberOfKeys - 1;
                K greater = node.getKey(last);
                if (key.compareTo(greater) > 0) {
                    node = node.getChild(numberOfKeys);
                    continue;
                }

                // Search internal nodes
                for (int i = 1; i < node.numberOfKeys(); i++) {
                    K prev = node.getKey(i - 1);
                    K next = node.getKey(i);
                    if (key.compareTo(prev) > 0 && key.compareTo(next) <= 0) {
                        node = node.getChild(i);
                        break;
                    }
                }
            }
        }

        size++;

        return true;
    }

    /**
     * The node's key size is greater than maxKeySize, split down the middle.
     *
     */
    private void split(Node<K, V> nodeToSplit) {
        Node<K, V> node = nodeToSplit;
        int numberOfKeys = node.numberOfKeys();
        int medianIndex = numberOfKeys / 2;
        K medianValue = node.getKey(medianIndex);

        Node<K, V> left = new Node<K, V>(null, maxKeySize, maxChildrenSize);
        for (int i = 0; i < medianIndex; i++) {
            left.addKey(node.getKey(i));
        }
        if (node.numberOfChildren() > 0) {
            for (int j = 0; j <= medianIndex; j++) {
                Node<K, V> c = node.getChild(j);
                left.addChild(c);
            }
        }

        Node<K, V> right = new Node<K, V>(null, maxKeySize, maxChildrenSize);
        for (int i = medianIndex + 1; i < numberOfKeys; i++) {
            right.addKey(node.getKey(i));
        }
        if (node.numberOfChildren() > 0) {
            for (int j = medianIndex + 1; j < node.numberOfChildren(); j++) {
                Node<K, V> c = node.getChild(j);
                right.addChild(c);
            }
        }

        if (node.parent == null) {
            // new root, height of tree is increased
            Node<K, V> newRoot = new Node<K, V>(null, maxKeySize, maxChildrenSize);
            newRoot.addKey(medianValue);
            node.parent = newRoot;
            root = newRoot;
            node = root;
            node.addChild(left);
            node.addChild(right);
        } else {
            // Move the median value up to the parent
            Node<K, V> parent = node.parent;
            parent.addKey(medianValue);
            parent.removeChild(node);
            parent.addChild(left);
            parent.addChild(right);

            if (parent.numberOfKeys() > maxKeySize) split(parent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K remove(K value) {
        K removed = null;
        Node<K, V> node = this.getNode(value);
        removed = remove(value, node);
        return removed;
    }

    /**
     * Remove the value from the Node and check invariants
     *
     * @param value K to remove from the tree
     * @param node  Node to remove value from
     * @return True if value was removed from the tree.
     */
    private K remove(K value, Node<K, V> node) {
        if (node == null) return null;

        K removed = null;
        int index = node.indexOf(value);
        removed = node.removeKey(value);
        if (node.numberOfChildren() == 0) {
            // leaf node
            if (node.parent != null && node.numberOfKeys() < minKeySize) {
                this.combined(node);
            } else if (node.parent == null && node.numberOfKeys() == 0) {
                // Removing root node with no keys or children
                root = null;
            }
        } else {
            // internal node
            Node<K, V> lesser = node.getChild(index);
            Node<K, V> greatest = this.getGreatestNode(lesser);
            K replaceValue = this.removeGreatestValue(greatest);
            node.addKey(replaceValue);
            if (greatest.parent != null && greatest.numberOfKeys() < minKeySize) {
                this.combined(greatest);
            }
            if (greatest.numberOfChildren() > maxChildrenSize) {
                this.split(greatest);
            }
        }

        size--;

        return removed;
    }

    /**
     * Remove greatest valued key from node.
     *
     * @param node to remove greatest value from.
     * @return value removed;
     */
    private K removeGreatestValue(Node<K, V> node) {
        K value = null;
        if (node.numberOfKeys() > 0) {
            value = node.removeKey(node.numberOfKeys() - 1);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(K value) {
        Node<K, V> node = getNode(value);
        return (node != null);
    }

    /**
     * Get the node with value.
     *
     * @param value to find in the tree.
     * @return Node<K, V> with value.
     */
    private Node<K, V> getNode(K value) {
        Node<K, V> node = root;
        while (node != null) {
            K lesser = node.getKey(0);
            if (value.compareTo(lesser) < 0) {
                if (node.numberOfChildren() > 0)
                    node = node.getChild(0);
                else
                    node = null;
                continue;
            }

            int numberOfKeys = node.numberOfKeys();
            int last = numberOfKeys - 1;
            K greater = node.getKey(last);
            if (value.compareTo(greater) > 0) {
                if (node.numberOfChildren() > numberOfKeys)
                    node = node.getChild(numberOfKeys);
                else
                    node = null;
                continue;
            }

            for (int i = 0; i < numberOfKeys; i++) {
                K currentValue = node.getKey(i);
                if (currentValue.compareTo(value) == 0) {
                    return node;
                }

                int next = i + 1;
                if (next <= last) {
                    K nextValue = node.getKey(next);
                    if (currentValue.compareTo(value) < 0 && nextValue.compareTo(value) > 0) {
                        if (next < node.numberOfChildren()) {
                            node = node.getChild(next);
                            break;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the greatest valued child from node.
     *
     * @param nodeToGet child with the greatest value.
     * @return Node<K, V> child with greatest value.
     */
    private Node<K, V> getGreatestNode(Node<K, V> nodeToGet) {
        Node<K, V> node = nodeToGet;
        while (node.numberOfChildren() > 0) {
            node = node.getChild(node.numberOfChildren() - 1);
        }
        return node;
    }

    /**
     * Combined children keys with parent when size is less than minKeySize.
     *
     * @param node with children to combined.
     * @return True if combined successfully.
     */
    private boolean combined(Node<K, V> node) {
        Node<K, V> parent = node.parent;
        int index = parent.indexOf(node);
        int indexOfLeftNeighbor = index - 1;
        int indexOfRightNeighbor = index + 1;

        Node<K, V> rightNeighbor = null;
        int rightNeighborSize = -minChildrenSize;
        if (indexOfRightNeighbor < parent.numberOfChildren()) {
            rightNeighbor = parent.getChild(indexOfRightNeighbor);
            rightNeighborSize = rightNeighbor.numberOfKeys();
        }

        // Try to borrow neighbor
        if (rightNeighbor != null && rightNeighborSize > minKeySize) {
            // Try to borrow from right neighbor
            K removeValue = rightNeighbor.getKey(0);
            int prev = getIndexOfPreviousValue(parent, removeValue);
            K parentValue = parent.removeKey(prev);
            K neighborValue = rightNeighbor.removeKey(0);
            node.addKey(parentValue);
            parent.addKey(neighborValue);
            if (rightNeighbor.numberOfChildren() > 0) {
                node.addChild(rightNeighbor.removeChild(0));
            }
        } else {
            Node<K, V> leftNeighbor = null;
            int leftNeighborSize = -minChildrenSize;
            if (indexOfLeftNeighbor >= 0) {
                leftNeighbor = parent.getChild(indexOfLeftNeighbor);
                leftNeighborSize = leftNeighbor.numberOfKeys();
            }

            if (leftNeighbor != null && leftNeighborSize > minKeySize) {
                // Try to borrow from left neighbor
                K removeValue = leftNeighbor.getKey(leftNeighbor.numberOfKeys() - 1);
                int prev = getIndexOfNextValue(parent, removeValue);
                K parentValue = parent.removeKey(prev);
                K neighborValue = leftNeighbor.removeKey(leftNeighbor.numberOfKeys() - 1);
                node.addKey(parentValue);
                parent.addKey(neighborValue);
                if (leftNeighbor.numberOfChildren() > 0) {
                    node.addChild(leftNeighbor.removeChild(leftNeighbor.numberOfChildren() - 1));
                }
            } else if (rightNeighbor != null && parent.numberOfKeys() > 0) {
                // Can't borrow from neighbors, try to combined with right neighbor
                K removeValue = rightNeighbor.getKey(0);
                int prev = getIndexOfPreviousValue(parent, removeValue);
                K parentValue = parent.removeKey(prev);
                parent.removeChild(rightNeighbor);
                node.addKey(parentValue);
                for (int i = 0; i < rightNeighbor.keysSize; i++) {
                    K v = rightNeighbor.getKey(i);
                    node.addKey(v);
                }
                for (int i = 0; i < rightNeighbor.childrenSize; i++) {
                    Node<K, V> c = rightNeighbor.getChild(i);
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfKeys() < minKeySize) {
                    // removing key made parent too small, combined up tree
                    this.combined(parent);
                } else if (parent.numberOfKeys() == 0) {
                    // parent no longer has keys, make this node the new root
                    // which decreases the height of the tree
                    node.parent = null;
                    root = node;
                }
            } else if (leftNeighbor != null && parent.numberOfKeys() > 0) {
                // Can't borrow from neighbors, try to combined with left neighbor
                K removeValue = leftNeighbor.getKey(leftNeighbor.numberOfKeys() - 1);
                int prev = getIndexOfNextValue(parent, removeValue);
                K parentValue = parent.removeKey(prev);
                parent.removeChild(leftNeighbor);
                node.addKey(parentValue);
                for (int i = 0; i < leftNeighbor.keysSize; i++) {
                    K v = leftNeighbor.getKey(i);
                    node.addKey(v);
                }
                for (int i = 0; i < leftNeighbor.childrenSize; i++) {
                    Node<K, V> c = leftNeighbor.getChild(i);
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfKeys() < minKeySize) {
                    // removing key made parent too small, combined up tree
                    this.combined(parent);
                } else if (parent.numberOfKeys() == 0) {
                    // parent no longer has keys, make this node the new root
                    // which decreases the height of the tree
                    node.parent = null;
                    root = node;
                }
            }
        }

        return true;
    }

    /**
     * Get the index of previous key in node.
     *
     * @param node  to find the previous key in.
     * @param value to find a previous value for.
     * @return index of previous key or -1 if not found.
     */
    private int getIndexOfPreviousValue(Node<K, V> node, K value) {
        for (int i = 1; i < node.numberOfKeys(); i++) {
            K k = node.getKey(i);
            if (k.compareTo(value) >= 0)
                return i - 1;
        }
        return node.numberOfKeys() - 1;
    }

    /**
     * Get the index of next key in node.
     *
     * @param node  to find the next key in.
     * @param value to find a next value for.
     * @return index of next key or -1 if not found.
     */
    private int getIndexOfNextValue(Node<K, V> node, K value) {
        for (int i = 0; i < node.numberOfKeys(); i++) {
            K k = node.getKey(i);
            if (k.compareTo(value) >= 0)
                return i;
        }
        return node.numberOfKeys() - 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate() {
        if (root == null) return true;
        return validateNode(root);
    }

    /**
     * Validate the node according to the B-Tree invariants.
     *
     * @param node to validate.
     * @return True if valid.
     */
    private boolean validateNode(Node<K, V> node) {
        int keySize = node.numberOfKeys();
        if (keySize > 1) {
            // Make sure the keys are sorted
            for (int i = 1; i < keySize; i++) {
                K p = node.getKey(i - 1);
                K n = node.getKey(i);
                if (p.compareTo(n) > 0)
                    return false;
            }
        }
        int childrenSize = node.numberOfChildren();
        if (node.parent == null) {
            // root
            if (keySize > maxKeySize) {
                // check max key size. root does not have a min key size
                return false;
            } else if (childrenSize == 0) {
                // if root, no children, and keys are valid
                return true;
            } else if (childrenSize < 2) {
                // root should have zero or at least two children
                return false;
            } else if (childrenSize > maxChildrenSize) {
                return false;
            }
        } else {
            // non-root
            if (keySize < minKeySize) {
                return false;
            } else if (keySize > maxKeySize) {
                return false;
            } else if (childrenSize == 0) {
                return true;
            } else if (keySize != (childrenSize - 1)) {
                // If there are chilren, there should be one more child then
                // keys
                return false;
            } else if (childrenSize < minChildrenSize) {
                return false;
            } else if (childrenSize > maxChildrenSize) {
                return false;
            }
        }

        Node<K, V> first = node.getChild(0);
        // The first child's last key should be less than the node's first key
        if (first.getKey(first.numberOfKeys() - 1).compareTo(node.getKey(0)) > 0)
            return false;

        Node<K, V> last = node.getChild(node.numberOfChildren() - 1);
        // The last child's first key should be greater than the node's last key
        if (last.getKey(0).compareTo(node.getKey(node.numberOfKeys() - 1)) < 0)
            return false;

        // Check that each node's first and last key holds it's invariance
        for (int i = 1; i < node.numberOfKeys(); i++) {
            K p = node.getKey(i - 1);
            K n = node.getKey(i);
            Node<K, V> c = node.getChild(i);
            if (p.compareTo(c.getKey(0)) > 0)
                return false;
            if (n.compareTo(c.getKey(c.numberOfKeys() - 1)) < 0)
                return false;
        }

        for (int i = 0; i < node.childrenSize; i++) {
            Node<K, V> c = node.getChild(i);
            boolean valid = this.validateNode(c);
            if (!valid)
                return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.Collection<K> toCollection() {
        return Collections.emptyList();
//        return (new JavaCompatibleBTree<K>(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }

    private static class Node<T extends Comparable<T>, U> {

        private T[] keys = null;
        private int keysSize = 0;
        private U[] values = null;

        private Node<T, U>[] children = null;
        private int childrenSize = 0;
        private boolean dirty = true;

        private Comparator<Node<T, U>> comparator = new Comparator<BTree.Node<T, U>>() {
            @Override
            public int compare(BTree.Node<T, U> arg0, BTree.Node<T, U> arg1) {
                return arg0.getKey(0).compareTo(arg1.getKey(0));
            }
        };

        protected Node<T, U> parent = null;

        private Node(Node<T, U> parent, int maxKeySize, int maxChildrenSize) {
            this.parent = parent;
            this.keys = (T[])new Comparable[maxKeySize + 1];
            this.keysSize = 0;
            this.values = (U[])new Object[maxKeySize + 1];

            this.children = new Node[maxChildrenSize + 1];
            this.childrenSize = 0;
        }

        private T getKey(int index) {
            return keys[index];
        }

        private int indexOf(T value) {
            for (int i = 0; i < keysSize; i++) {
                if (keys[i].equals(value)) return i;
            }
            return -1;
        }

        private void addKey(T value) {
            keys[keysSize++] = value;
            Arrays.sort(keys, 0, keysSize);
        }

        private T removeKey(T value) {
            T removed = null;
            boolean found = false;
            if (keysSize == 0) return null;
            for (int i = 0; i < keysSize; i++) {
                if (keys[i].equals(value)) {
                    found = true;
                    removed = keys[i];
                } else if (found) {
                    // shift the rest of the keys down
                    keys[i - 1] = keys[i];
                }
            }
            if (found) {
                keysSize--;
                keys[keysSize] = null;
            }
            return removed;
        }

        private T removeKey(int index) {
            if (index >= keysSize)
                return null;
            T value = keys[index];
            for (int i = index + 1; i < keysSize; i++) {
                // shift the rest of the keys down
                keys[i - 1] = keys[i];
            }
            keysSize--;
            keys[keysSize] = null;
            return value;
        }

        private int numberOfKeys() {
            return keysSize;
        }

        private Node<T, U> getChild(int index) {
            if (index >= childrenSize)
                return null;
            return children[index];
        }

        private int indexOf(Node<T, U> child) {
            for (int i = 0; i < childrenSize; i++) {
                if (children[i].equals(child))
                    return i;
            }
            return -1;
        }

        private boolean addChild(Node<T, U> child) {
            child.parent = this;
            children[childrenSize++] = child;
            Arrays.sort(children, 0, childrenSize, comparator);
            return true;
        }

        private boolean removeChild(Node<T, U> child) {
            boolean found = false;
            if (childrenSize == 0)
                return found;
            for (int i = 0; i < childrenSize; i++) {
                if (children[i].equals(child)) {
                    found = true;
                } else if (found) {
                    // shift the rest of the keys down
                    children[i - 1] = children[i];
                }
            }
            if (found) {
                childrenSize--;
                children[childrenSize] = null;
            }
            return found;
        }

        private Node<T, U> removeChild(int index) {
            if (index >= childrenSize)
                return null;
            Node<T, U> value = children[index];
            children[index] = null;
            for (int i = index + 1; i < childrenSize; i++) {
                // shift the rest of the keys down
                children[i - 1] = children[i];
            }
            childrenSize--;
            children[childrenSize] = null;
            return value;
        }

        private int numberOfChildren() {
            return childrenSize;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("keys=[");
            for (int i = 0; i < numberOfKeys(); i++) {
                T value = getKey(i);
                builder.append(value);
                if (i < numberOfKeys() - 1)
                    builder.append(", ");
            }
            builder.append("]\n");

            if (parent != null) {
                builder.append("parent=[");
                for (int i = 0; i < parent.numberOfKeys(); i++) {
                    T value = parent.getKey(i);
                    builder.append(value);
                    if (i < parent.numberOfKeys() - 1)
                        builder.append(", ");
                }
                builder.append("]\n");
            }

            if (children != null) {
                builder.append("keySize=").append(numberOfKeys()).append(" children=").append(numberOfChildren()).append("\n");
            }

            return builder.toString();
        }
    }

    private static class TreePrinter {

        public static <K extends Comparable<K>, V> String getString(BTree<K, V> tree) {
            if (tree.root == null) return "Tree has no nodes.";
            return getString(tree.root, "", true);
        }

        private static <T extends Comparable<T>, U> String getString(Node<T, U> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix).append((isTail ? "└── " : "├── "));
            for (int i = 0; i < node.numberOfKeys(); i++) {
                T value = node.getKey(i);
                builder.append(value);
                if (i < node.numberOfKeys() - 1)
                    builder.append(", ");
            }
            builder.append("\n");

            if (node.children != null) {
                for (int i = 0; i < node.numberOfChildren() - 1; i++) {
                    Node<T, U> obj = node.getChild(i);
                    builder.append(getString(obj, prefix + (isTail ? "    " : "│   "), false));
                }
                if (node.numberOfChildren() >= 1) {
                    Node<T, U> obj = node.getChild(node.numberOfChildren() - 1);
                    builder.append(getString(obj, prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }

    public java.util.Iterator<Map.Entry<K,V>> iterator() {
        new BTreeIterator<K,V>(this.tree);
        return Collections.EMPTY_LIST.iterator();
    }

    private static class BTreeIterator<K extends Comparable<K>, V> implements java.util.Iterator<Map.Entry<K,V>> {

        private BTree<K,V> tree = null;
        private Node<C> lastNode = null;
        private K lastValue = null;
        private int index = 0;
        private Deque<edu.utexas.arlut.ciads.jai.BTree.Node<C>> toVisit = new ArrayDeque<edu.utexas.arlut.ciads.jai.BTree.Node<C>>();

        protected BTreeIterator(BTree<K,V> tree) {
            this.tree = tree;
            if (tree.root != null && tree.root.keysSize > 0) {
                toVisit.add(tree.root);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            if ((lastNode != null && index < lastNode.keysSize) || (toVisit.size() > 0)) return true;
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public C next() {
            if (lastNode != null && (index < lastNode.keysSize)) {
                lastValue = lastNode.getKey(index++);
                return lastValue;
            }
            while (toVisit.size() > 0) {
                // Go thru the current nodes
                edu.utexas.arlut.ciads.jai.BTree.Node<C> n = toVisit.pop();

                // Add non-null children
                for (int i = 0; i < n.childrenSize; i++) {
                    toVisit.add(n.getChild(i));
                }

                // Update last node (used in remove method)
                index = 0;
                lastNode = n;
                lastValue = lastNode.getKey(index++);
                return lastValue;
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            if (lastNode != null && lastValue != null) {
                // On remove, reset the iterator (very inefficient, I know)
                tree.remove(lastValue, lastNode);

                lastNode = null;
                lastValue = null;
                index = 0;
                toVisit.clear();
                if (tree.root != null && tree.root.keysSize > 0) {
                    toVisit.add(tree.root);
                }
            }
        }
    }

    public static class JavaCompatibleBTree<K extends Comparable<K>, V> extends java.util.AbstractCollection<T> {

        private BTree<T> tree = null;

        public JavaCompatibleBTree(BTree<T> tree) {
            this.tree = tree;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(T value) {
            return tree.add(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object value) {
            return (tree.remove((T)value) != null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object value) {
            return tree.contains((T)value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return tree.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.util.Iterator<K> iterator() {
            return (new BTreeIterator<K>(this.tree));
        }

        private static class BTreeIterator<K extends Comparable<K>> implements java.util.Iterator<K> {

            private BTree<K, V> tree = null;
            private BTree.Node<K, V> lastNode = null;
            private K lastValue = null;
            private int index = 0;
            private Deque<BTree.Node<K, V>> toVisit = new ArrayDeque<BTree.Node<K, V>>();

            protected BTreeIterator(BTree<K, V> tree) {
                this.tree = tree;
                if (tree.root != null && tree.root.keysSize > 0) {
                    toVisit.add(tree.root);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                if ((lastNode != null && index < lastNode.keysSize) || (toVisit.size() > 0)) return true;
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public K next() {
                if (lastNode != null && (index < lastNode.keysSize)) {
                    lastValue = lastNode.getKey(index++);
                    return lastValue;
                }
                while (toVisit.size() > 0) {
                    // Go thru the current nodes
                    BTree.Node<K, V> n = toVisit.pop();

                    // Add non-null children
                    for (int i = 0; i < n.childrenSize; i++) {
                        toVisit.add(n.getChild(i));
                    }

                    // Update last node (used in remove method)
                    index = 0;
                    lastNode = n;
                    lastValue = lastNode.getKey(index++);
                    return lastValue;
                }
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                if (lastNode != null && lastValue != null) {
                    // On remove, reset the iterator (very inefficient, I know)
                    tree.remove(lastValue, lastNode);

                    lastNode = null;
                    lastValue = null;
                    index = 0;
                    toVisit.clear();
                    if (tree.root != null && tree.root.keysSize > 0) {
                        toVisit.add(tree.root);
                    }
                }
            }
        }
    }
}