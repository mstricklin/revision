// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev;

import static com.google.common.collect.Maps.newHashMap;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Modified from java-algorithms-implementation
 * at https://code.google.com/archive/p/java-algorithms-implementation/
 * released under Apache license 2.0
 * <p>
 * B-tree is a tree data structure that keeps data sorted and allows searches,
 * sequential access, insertions, and deletions in logarithmic time. The B-tree
 * is a generalization of a binary search tree in that a node can have more than
 * two children. Unlike self-balancing binary search trees, the B-tree is
 * optimized for systems that read and write large blocks of data. It is
 * commonly used in databases and file-systems.
 * <p>
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 * @author Justin Wetherell <phishman3579@gmail.com>
 * @see <a href="https://en.wikipedia.org/wiki/B-tree">B-Tree (Wikipedia)</a>
 * <p>
 * B-tree is a tree data structure that keeps data sorted and allows searches,
 * sequential access, insertions, and deletions in logarithmic time. The B-tree
 * is a generalization of a binary search tree in that a node can have more than
 * two children. Unlike self-balancing binary search trees, the B-tree is
 * optimized for systems that read and write large blocks of data. It is
 * commonly used in databases and file-systems.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/B-tree">B-Tree (Wikipedia)</a>
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
 *
 */

/**
 We use a b-tree here because we need a segmented index, to reduce the cost of iterating the index.
 Each modification requires incrementing the index, a 2-tree would require incrementing log2(n)
 sub-indices. A B-tree of splay-order B requires (if sparse or unbalanced) n/B increments,
 (if balanced and full) logB(n) increments.

 The minimum size of copies (indices to copy*size of index) is e=2.718, so the optimum integral size
 is a 2-3 tree. This is without overhead such as pointers to parent nodes. With a single additional
 pointer per node, the optimum splay is closer to 3.

 Load is (if B=splay, p=pointer size)
 Bp (for keys) + Bp (for entries) + (B+1)p (for children) + p  (for parent pointer)
 additional: int (for entry size) + int (for children size)
 */
@SuppressWarnings("unchecked")
@Slf4j
public class BTree<K extends Comparable<K>, V> implements ITree<K, V>, Iterable<Map.Entry<K, V>> {

    static Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
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

    private Node<K, V> newNode(Node<K, V> parent) {
        return new Node<K, V>(null, maxKeySize, maxChildrenSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(K key, V value) {
        if (root == null) {
            root = newNode(null);
            root.put(key, value);
        } else {
            Node<K, V> node = root;
            while (node != null) {
                if (node.numberOfChildren() == 0) {
                    // mutate...
                    node.put(key, value);
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
     */
    private void split(Node<K, V> nodeToSplit) {
        log.info("Splitting {}", nodeToSplit.id);
        Node<K, V> node = nodeToSplit;
        int numberOfKeys = node.numberOfKeys();
        int medianIndex = numberOfKeys / 2;
        Map.Entry medianEntry = node.getEntry(medianIndex);

        // split left
        Node<K, V> left = newNode(null);
        log.info("new left {}", left.id);
        left.assignFrom(node, 0, medianIndex);

        // split right
        Node<K, V> right = newNode(null);
        log.info("new right {}", right.id);
        right.assignFrom(node, medianIndex + 1, numberOfKeys);

//        if (node.parent == null) {
        if (root == node) {
            // new root, height of tree is increased
            Node<K, V> newRoot = newNode(null);
            newRoot.addEntry(medianEntry);
            node.parent = newRoot;
            root = newRoot;
            node = root;
            node.addChild(left);
            node.addChild(right);
        } else {
            // Move the median value up to the parent
            Node<K, V> parent = node.parent;
            parent.addEntry(medianEntry);
            parent.removeChild(node);
            parent.addChild(left);
            parent.addChild(right);

            if (parent.numberOfKeys() > maxKeySize) split(parent);
            // mark dirty from parent up...
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K remove(K key) {
        K removed = null;
        Node<K, V> node = this.getNode(key);
        removed = remove(key, node);
        return removed;
    }

    /**
     * Remove the value from the Node and check invariants
     *
     * @param key  K to remove from the tree
     * @param node Node to remove value from
     * @return True if value was removed from the tree.
     */
    private K remove(K key, Node<K, V> node) {
        if (node == null) return null;

        K removed = null;
        int index = node.indexOf(key);
        removed = node.removeKey(key);
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
            Map.Entry<K, V> replaceEntry = this.removeGreatestEntry(greatest);
            node.addEntry(replaceEntry);
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
    private Map.Entry<K, V> removeGreatestEntry(Node<K, V> node) {
        Map.Entry<K, V> value = null;
        if (node.numberOfKeys() > 0) {
            value = node.removeEntry(node.numberOfKeys() - 1);
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
    public V get(K key) {
        Node<K, V> node = getNode(key);
        return node.getValue(key);
    }
    @Override
    public boolean contains(K key) {
        Node<K, V> node = getNode(key);
        return (node != null);
    }

    private Comparator<Node<K, V>> C = new Comparator<Node<K, V>>() {
        @Override
        public int compare(Node<K, V> kvNode, Node<K, V> t1) {
            return 0;
        }
    };

    private Node<K, V> getNode(Node<K, V> start, K key) {
        int i = start.find(key);
//        K[] keys = (K[])new Comparable[start.keysSize];
//        Arrays.setAll(keys, i->start.entries[i].getKey());
//        Arrays.binarySearch(start.entries, 0, start.keysSize, key);
        return null;
    }
    private Comparator<Node<K, V>> C2 = (arg0, arg1) -> arg0.getKey(0).compareTo(arg1.getKey(0));


    /**
     * Get the node with key.
     *
     * @param key to find in the tree.
     * @return Node<K, V> with value.
     */
    private Node<K, V> getNode(K key) {
        Node<K, V> node = root;
        while (node != null) {
            K lesser = node.getKey(0);
            if (key.compareTo(lesser) < 0) {
                if (node.numberOfChildren() > 0)
                    node = node.getChild(0);
                else
                    node = null;
                continue;
            }

            int numberOfKeys = node.numberOfKeys();
            int last = numberOfKeys - 1;
            K greater = node.getKey(last);
            if (key.compareTo(greater) > 0) {
                if (node.numberOfChildren() > numberOfKeys)
                    node = node.getChild(numberOfKeys);
                else
                    node = null;
                continue;
            }

            for (int i = 0; i < numberOfKeys; i++) {
                K currentValue = node.getKey(i);
                if (currentValue.compareTo(key) == 0) {
                    return node;
                }

                int next = i + 1;
                if (next <= last) {
                    K nextValue = node.getKey(next);
                    if (currentValue.compareTo(key) < 0 && nextValue.compareTo(key) > 0) {
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
            Map.Entry<K, V> parentEntry = parent.removeEntry(prev);
            Map.Entry<K, V> neighborEntry = rightNeighbor.removeEntry(prev);
            node.addEntry(parentEntry);
            parent.addEntry(neighborEntry);
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
                Map.Entry<K, V> parentEntry = parent.removeEntry(prev);
                Map.Entry<K, V> neighborEntry = leftNeighbor.removeEntry(leftNeighbor.numberOfKeys() - 1);
                node.addEntry(parentEntry);
                parent.addEntry(neighborEntry);
                if (leftNeighbor.numberOfChildren() > 0) {
                    node.addChild(leftNeighbor.removeChild(leftNeighbor.numberOfChildren() - 1));
                }
            } else if (rightNeighbor != null && parent.numberOfKeys() > 0) {
                // Can't borrow from neighbors, try to combined with right neighbor
                K removeValue = rightNeighbor.getKey(0);
                int prev = getIndexOfPreviousValue(parent, removeValue);

                Map.Entry<K, V> parentEntry = parent.removeEntry(prev);
                parent.removeChild(rightNeighbor);
                node.addEntry(parentEntry);
                for (int i = 0; i < rightNeighbor.keysSize; i++) {
                    Map.Entry<K, V> e = rightNeighbor.getEntry(i);
                    node.addEntry(e);
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
                Map.Entry<K, V> parentEntry = parent.removeEntry(prev);
                parent.removeChild(leftNeighbor);
                node.addEntry(parentEntry);
                for (int i = 0; i < leftNeighbor.keysSize; i++) {
                    Map.Entry<K, V> e = leftNeighbor.getEntry(i);
                    node.addEntry(e);
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
    public String toString() {
        return TreePrinter.getString(this);
    }

    private static class Node<T extends Comparable<T>, U> {
        private static int ID = 0;
        private int id;
        private int keysSize = 0;
        private T[] keys = null;
        private U[] values = null;


        private Node<T, U>[] children = null;
        private int childrenSize = 0;
        private boolean dirty = true;

        private Comparator<Node<T, U>> comparator = (arg0, arg1) -> arg0.getKey(0).compareTo(arg1.getKey(0));

        protected Node<T, U> parent = null;

        private Node(Node<T, U> parent, int maxKeySize, int maxChildSize) {
            id = ID++;
//            log.info("new node {}", id);
            this.parent = parent;
            this.keysSize = 0;
            this.keys = (T[])new Comparable[maxKeySize + 1];
            this.values = (U[])new Object[maxKeySize + 1];

            this.children = new Node[maxChildSize + 1];
            this.childrenSize = 0;
        }


        private int find(T key) {
            return Arrays.binarySearch(keys, 0, keysSize, key, null);
//            for (int i=0; i<keysSize; i++) {
//                if (getKey(i).equals(key))
//                    return i;
//            }
//            return -1;
        }

        private Map.Entry<T, U> getEntry(int i) {
            return new SimpleImmutableEntry<>(keys[i], values[i]);
//            return entries[i];
        }

        private T getKey(int i) {
            return keys[i];
//            return entries[i].getKey();
        }
        private U getValue(int i) {
            return values[i];
//            return entries[i].getValue();
        }
        private U getValue(T key) {
            int i = Arrays.binarySearch(keys, 0, keysSize, key, null);
            if (i >= 0)
                return values[i];
            return null;
//            for (int i = 0; i < keysSize; i++) {
//                if (entries[i].getKey().equals(key))
//                    return entries[i].getValue();
//            }
//            return null;
        }

        private int indexOf(T key) {
            return Arrays.binarySearch(keys, 0, keysSize, key, null);
//            for (int i = 0; i < keysSize; i++) {
//                // Arrays.binarySearch?
//                if (entries[i].getKey().equals(key))
//                    return i;
//            }
//            return -1;
        }

        private Node<T, U> put(T key, U value) {
            // make dirty copy
            // if contains key
            //    replace entry
            // else
            int i = indexOf(key);
            if (i < 0) {
                i = -(i + 1);
                log.info("Not contained {}:{}, entry point {}", key, value, i);
                // move over
                for (int k = keysSize - 1; k >= i; k--) {
                    keys[k + 1] = keys[k];
                    values[k + 1] = values[k];
                }
                keys[i] = key;
                values[i] = value;
                keysSize++;
            } else { // already has, replace
                // mark dirty
                values[i] = value;
            }
            log.info("put {}=>{}", key, value);
//            addEntry(new SimpleImmutableEntry<>(key, value));
            return this;
        }
        private void assignFrom(Node<T, U> from, int start, int end) {
            for (int i = start; i < end; i++) {
                keys[keysSize] = from.keys[i];
                values[keysSize] = from.values[i];
                keysSize++;
//                entries[keysSize++] = from.entries[i];
            }
//            Arrays.sort(entries, 0, keysSize, Map.Entry.comparingByKey());

            if (numberOfChildren() > 0) {
                for (int j = start; j <= end; j++) {
                    Node<T, U> c = getChild(j);
                    addChild(c);
                }
            }
        }
        private Node<T, U> addEntry(Map.Entry<T, U> e) {
            log.info("addEntry {}", e);
//            keys[keysSize] = e.getKey();
//            values[keysSize] = e.getValue();
            put(e.getKey(), e.getValue());
//            entries[keysSize++] = e;
//            Arrays.sort(entries, 0, keysSize, Map.Entry.comparingByKey());
            return this;
        }
        private T removeKey(T key) {
            T removed = null;
            boolean found = false;
            if (keysSize == 0) return null;
            int idx = find(key);
            if (idx >= 0) {
                log.info("remove {}=>{}", key, values[idx]);

                removed = keys[idx];
                for (int k = idx; k < keysSize - 1; k++) {
                    keys[k] = keys[k + 1];
                    values[k] = values[k + 1];
                }
                keysSize--;
                keys[keysSize] = null;
                values[keysSize] = null;
            }

//            for (int i = 0; i < keysSize; i++) {
//                if (entries[i].getKey().equals(key)) {
//                    found = true;
//                    removed = entries[i].getKey();
//                } else if (found) {
//                    // TODO: System.arraycopy();
//                    // shift the rest of the keys down
//                    entries[i - 1] = entries[i];
//                }
//            }
//            if (found) {
//                keysSize--;
//                entries[keysSize] = null;
//            }
            return removed;
        }
        private Map.Entry<T, U> removeEntry(int index) {
            if (index >= keysSize)
                return null;
//            Map.Entry<T, U> removed = entries[index];
            Map.Entry<T, U> removed = new AbstractMap.SimpleImmutableEntry(keys[index], values[index]);
            // shift the rest of the keys down
            for (int i = index + 1; i < keysSize; i++) {
//                entries[i - 1] = entries[i];
                keys[i - 1] = keys[i];
                values[i - 1] = values[i];
            }
            keysSize--;
            keys[keysSize] = null;
            values[keysSize] = null;
            return removed;
        }

        private int numberOfKeys() {
            return keysSize;
        }
        // =================================
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

        private String kvString() {
            return IntStream.range(0, keysSize)
                                .mapToObj(i -> new AbstractMap.SimpleEntry<>(keys[i], values[i]))
                                .map(e -> e.toString())
                                .collect(Collectors.joining(", "));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append(Integer.toString(id))
                   .append(" ")
                   .append("entries=[").append(kvString())
                   .append("]\n");

            if (parent != null) {
                builder.append("parent=[").append(parent.kvString()).append("]\n");
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
//            COMMA_JOINER.appendTo(builder, node.entries);
            String s = IntStream.range(0, node.keysSize)
                                .mapToObj(i -> new AbstractMap.SimpleEntry<>(node.keys[i], node.values[i]))
                                .map(e -> e.toString())
                                .collect(Collectors.joining(", "));
            builder.append(s);


            builder.append(" (" + node.id + (node.dirty ? "X" : "") + ")\n");

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

    public Stream<Map.Entry<K, V>> stream() {
        return Streams.stream(this);
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return new BTreeIterator<>(this);
    }

    private static class BTreeIterator<K extends Comparable<K>, V> implements Iterator<Map.Entry<K, V>> {

        private BTree<K, V> tree = null;
        private Node<K, V> lastNode = null;
        private Map.Entry<K, V> lastValue = null;
        private int index = 0;
        private Deque<Node<K, V>> toVisit = new ArrayDeque<>();

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
        public Map.Entry<K, V> next() {
            if (lastNode != null && (index < lastNode.keysSize)) {
                lastValue = lastNode.getEntry(index++);
                return lastValue;
            }
            while (toVisit.size() > 0) {
                // Go thru the current nodes
                Node<K, V> n = toVisit.pop();

                // Add non-null children
                for (int i = 0; i < n.childrenSize; i++) {
                    toVisit.add(n.getChild(i));
                }

                // Update last node (used in remove method)
                index = 0;
                lastNode = n;
                lastValue = lastNode.getEntry(index++);
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
                tree.remove(lastValue.getKey(), lastNode);

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