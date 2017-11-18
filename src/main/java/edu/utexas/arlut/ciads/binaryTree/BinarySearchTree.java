package edu.utexas.arlut.ciads.binaryTree;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Queue;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * A binary search tree (BST), which may sometimes also be called an ordered or
 * sorted binary tree, is a node-based binary tree data structure which has the
 * following properties: 1) The left subtree of a node contains only nodes with
 * keys less than the node's key. 2) The right subtree of a node contains only
 * nodes with keys greater than the node's key. 3) Both the left and right
 * subtrees must also be binary search trees.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Binary_search_tree">Binary Search Tree (Wikipedia)</a>
 * <br>
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
@SuppressWarnings("unchecked")
@Slf4j
public class BinarySearchTree<T extends Comparable<T>> implements ITree<T> {

    private int modifications = 0;

    protected static final Random RANDOM = new Random();

    protected Node<T> root = null;
    protected int size = 0;
    protected INodeCreator<T> creator = null;

    public enum DepthFirstSearchOrder {
        inOrder, preOrder, postOrder
    }

    /**
     * Default constructor.
     */
    public BinarySearchTree() {
        this.creator = new INodeCreator<T>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public Node<T> createNewNode(Node<T> parent, T id) {
                return (new Node<T>(parent, id));
            }
        };
    }

    /**
     * Constructor with external Node creator.
     */
    public BinarySearchTree(INodeCreator<T> creator) {
        this.creator = creator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(T value) {
        Node<T> nodeAdded = this.addValue(value);
        return (nodeAdded != null);
    }

    /**
     * Add value to the tree and return the Node that was added. Tree can
     * contain multiple equal values.
     * 
     * @param value
     *            T to put to the tree.
     * @return Node<T> which was added to the tree.
     */
    protected Node<T> addValue(T value) {
        Node<T> newNode = this.creator.createNewNode(null, value);

        // If root is null, assign
        if (root == null) {
            root = newNode;
            size++;
            return newNode;
        }

        Node<T> node = root;
        while (node != null) {
            if (newNode.id.compareTo(node.id) <= 0) {
                // Less than or equal to goes left
                if (node.getLesser() == null) {
                    // New left node
                    node.setLesser(newNode);
                    newNode.setParent(node);
                    size++;
                    return newNode;
                }
                node = node.getLesser();
            } else {
                // Greater than goes right
                if (node.greater == null) {
                    // New right node
                    node.greater = newNode;
                    newNode.setParent(node);
                    size++;
                    return newNode;
                }
                node = node.greater;
            }
        }

        return newNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(T value) {
        Node<T> node = getNode(value);
        return (node != null);
    }

    /**
     * Locate T in the tree.
     * 
     * @param value
     *            T to locate in the tree.
     * @return Node<T> representing first reference of value in tree or NULL if
     *         not found.
     */
    protected Node<T> getNode(T value) {
        Node<T> node = root;
        while (node != null && node.id != null) {
            if (value.compareTo(node.id) < 0) {
                node = node.getLesser();
            } else if (value.compareTo(node.id) > 0) {
                node = node.greater;
            } else if (value.compareTo(node.id) == 0) {
                return node;
            }
        }
        return null;
    }

    /**
     * Rotate tree left at sub-tree rooted at node.
     * 
     * @param node
     *            Root of tree to rotate left.
     */
    protected void rotateLeft(Node<T> node) {
        Node<T> parent = node.getParent();
        Node<T> greater = node.greater;
        Node<T> lesser = greater.getLesser();

        greater.setLesser(node);
        node.setParent(greater);

        node.greater = lesser;

        if (lesser != null)
            lesser.setParent(node);

        if (parent!=null) {
            if (node == parent.getLesser()) {
                parent.setLesser(greater);
            } else if (node == parent.greater) {
                parent.greater = greater;
            } else {
                throw new RuntimeException("Yikes! I'm not related to my parent. " + node.toString());
            }
            greater.setParent(parent);
        } else {
            root = greater;
            root.setParent(null);
        }
    }

    /**
     * Rotate tree right at sub-tree rooted at node.
     * 
     * @param node
     *            Root of tree to rotate right.
     */
    protected void rotateRight(Node<T> node) {
        Node<T> parent = node.getParent();
        Node<T> lesser = node.getLesser();
        Node<T> greater = lesser.greater;

        lesser.greater = node;
        node.setParent(lesser);

        node.setLesser(greater);

        if (greater != null)
            greater.setParent(node);

        if (parent!=null) {
            if (node == parent.getLesser()) {
                parent.setLesser(lesser);
            } else if (node == parent.greater) {
                parent.greater = lesser;
            } else {
                throw new RuntimeException("Yikes! I'm not related to my parent. " + node.toString());
            }
            lesser.setParent(parent);
        } else {
            root = lesser;
            root.setParent(null);
        }
    }

    /**
     * Get greatest node in sub-tree rooted at startingNode. The search does not
     * include startingNode in it's results.
     * 
     * @param startingNode
     *            Root of tree to search.
     * @return Node<T> which represents the greatest node in the startingNode
     *         sub-tree or NULL if startingNode has no greater children.
     */
    protected Node<T> getGreatest(Node<T> startingNode) {
        if (startingNode == null)
            return null;

        Node<T> greater = startingNode.greater;
        while (greater != null && greater.id != null) {
            Node<T> node = greater.greater;
            if (node != null && node.id != null)
                greater = node;
            else
                break;
        }
        return greater;
    }

    /**
     * Get least node in sub-tree rooted at startingNode. The search does not
     * include startingNode in it's results.
     * 
     * @param startingNode
     *            Root of tree to search.
     * @return Node<T> which represents the least node in the startingNode
     *         sub-tree or NULL if startingNode has no lesser children.
     */
    protected Node<T> getLeast(Node<T> startingNode) {
        if (startingNode == null)
            return null;

        Node<T> lesser = startingNode.getLesser();
        while (lesser != null && lesser.id != null) {
            Node<T> node = lesser.getLesser();
            if (node != null && node.id != null)
                lesser = node;
            else
                break;
        }
        return lesser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T remove(T value) {
        Node<T> nodeToRemove = this.removeValue(value);
        return ((nodeToRemove!=null)?nodeToRemove.id:null);
    }

    /**
     * Remove first occurrence of value in the tree.
     * 
     * @param value
     *            T to remove from the tree.
     * @return Node<T> which was removed from the tree.
     */
    protected Node<T> removeValue(T value) {
        Node<T> nodeToRemoved = this.getNode(value);
        if (nodeToRemoved != null) 
            nodeToRemoved = removeNode(nodeToRemoved);
        return nodeToRemoved;
    }

    /**
     * Remove the node using a replacement
     * 
     * @param nodeToRemoved
     *            Node<T> to remove from the tree.
     * @return nodeRemove
     *            Node<T> removed from the tree, it can be different
     *            then the parameter in some cases.
     */
    protected Node<T> removeNode(Node<T> nodeToRemoved) {
        if (nodeToRemoved != null) {
            Node<T> replacementNode = this.getReplacementNode(nodeToRemoved);
            replaceNodeWithNode(nodeToRemoved, replacementNode);
        }
        return nodeToRemoved;
    }

    /**
     * Get the proper replacement node according to the binary search tree
     * algorithm from the tree.
     * 
     * @param nodeToRemoved
     *            Node<T> to find a replacement for.
     * @return Node<T> which can be used to replace nodeToRemoved. nodeToRemoved
     *         should NOT be NULL.
     */
    protected Node<T> getReplacementNode(Node<T> nodeToRemoved) {
        Node<T> replacement = null;
        if (nodeToRemoved.greater != null && nodeToRemoved.getLesser() != null) {
            // Two children.
            // Add some randomness to deletions, so we don't always use the
            // greatest/least on deletion
            if (modifications % 2 != 0) {
                replacement = this.getGreatest(nodeToRemoved.getLesser());
                if (replacement == null)
                    replacement = nodeToRemoved.getLesser();
            } else {
                replacement = this.getLeast(nodeToRemoved.greater);
                if (replacement == null)
                    replacement = nodeToRemoved.greater;
            }
            modifications++;
        } else if (nodeToRemoved.getLesser() != null && nodeToRemoved.greater == null) {
            // Using the less subtree
            replacement = nodeToRemoved.getLesser();
        } else if (nodeToRemoved.greater != null && nodeToRemoved.getLesser() == null) {
            // Using the greater subtree (there is no lesser subtree, no refactoring)
            replacement = nodeToRemoved.greater;
        }
        return replacement;
    }

    /**
     * Replace nodeToRemoved with replacementNode in the tree.
     * 
     * @param nodeToRemoved
     *            Node<T> to remove replace in the tree. nodeToRemoved should
     *            NOT be NULL.
     * @param replacementNode
     *            Node<T> to replace nodeToRemoved in the tree. replacementNode
     *            can be NULL.
     */
    protected void replaceNodeWithNode(Node<T> nodeToRemoved, Node<T> replacementNode) {
        if (replacementNode != null) {
            // Save for later
            Node<T> replacementNodeLesser = replacementNode.getLesser();
            Node<T> replacementNodeGreater = replacementNode.greater;

            // Replace replacementNode's branches with nodeToRemove's branches
            Node<T> nodeToRemoveLesser = nodeToRemoved.getLesser();
            if (nodeToRemoveLesser != null && nodeToRemoveLesser != replacementNode) {
                replacementNode.setLesser(nodeToRemoveLesser);
                nodeToRemoveLesser.setParent(replacementNode);
            }
            Node<T> nodeToRemoveGreater = nodeToRemoved.greater;
            if (nodeToRemoveGreater != null && nodeToRemoveGreater != replacementNode) {
                replacementNode.greater = nodeToRemoveGreater;
                nodeToRemoveGreater.setParent(replacementNode);
            }

            // Remove link from replacementNode's parent to replacement
            Node<T> replacementParent = replacementNode.getParent();
            if (replacementParent != null && replacementParent != nodeToRemoved) {
                Node<T> replacementParentLesser = replacementParent.getLesser();
                Node<T> replacementParentGreater = replacementParent.greater;
                if (replacementParentLesser != null && replacementParentLesser == replacementNode) {
                    replacementParent.setLesser(replacementNodeGreater);
                    if (replacementNodeGreater != null)
                        replacementNodeGreater.setParent(replacementParent);
                } else if (replacementParentGreater != null && replacementParentGreater == replacementNode) {
                    replacementParent.greater = replacementNodeLesser;
                    if (replacementNodeLesser != null)
                        replacementNodeLesser.setParent(replacementParent);
                }
            }
        }

        // Update the link in the tree from the nodeToRemoved to the
        // replacementNode
        Node<T> parent = nodeToRemoved.getParent();
        if (parent == null) {
            // Replacing the root node
            root = replacementNode;
            if (root != null)
                root.setParent(null);
        } else if (parent.getLesser() != null && (parent.getLesser().id.compareTo(nodeToRemoved.id) == 0)) {
            parent.setLesser(replacementNode);
            if (replacementNode != null)
                replacementNode.setParent(parent);
        } else if (parent.greater != null && (parent.greater.id.compareTo(nodeToRemoved.id) == 0)) {
            parent.greater = replacementNode;
            if (replacementNode != null)
                replacementNode.setParent(parent);
        }
        size--;
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
     * Validate the node for all Binary Search Tree invariants.
     * 
     * @param node
     *            Node<T> to validate in the tree. node should NOT be NULL.
     * @return True if the node is valid.
     */
    protected boolean validateNode(Node<T> node) {
        Node<T> lesser = node.getLesser();
        Node<T> greater = node.greater;

        boolean lesserCheck = true;
        if (lesser != null && lesser.id != null) {
            lesserCheck = (lesser.id.compareTo(node.id) <= 0);
            if (lesserCheck)
                lesserCheck = validateNode(lesser);
        }
        if (!lesserCheck)
            return false;

        boolean greaterCheck = true;
        if (greater != null && greater.id != null) {
            greaterCheck = (greater.id.compareTo(node.id) > 0);
            if (greaterCheck)
                greaterCheck = validateNode(greater);
        }
        return greaterCheck;
    }

    /**
     * Get an array representation of the tree in breath first search order.
     * 
     * @return breath first search sorted array representing the tree.
     */
    public T[] getBFS() { 
        return getBFS(this.root, this.size);
    }

    /**
     * Get an array representation of the tree in breath first search order.
     * 
     * @param start rooted node
     * @param size of tree rooted at start
     * 
     * @return breath first search sorted array representing the tree.
     */
    public static <T extends Comparable<T>> T[] getBFS(Node<T> start, int size) {
        final Queue<Node<T>> queue = new ArrayDeque<Node<T>>();
        final T[] values = (T[])Array.newInstance(start.id.getClass(), size);
        int count = 0;
        Node<T> node = start;
        while (node != null) {
            values[count++] = node.id;
            if (node.getLesser() != null)
                queue.add(node.getLesser());
            if (node.greater != null)
                queue.add(node.greater);
            if (!queue.isEmpty())
                node = queue.remove();
            else
                node = null;
        }
        return values;
    }

    /**
     * Get an array representation of the tree in level order.
     * 
     * @return level order sorted array representing the tree.
     */
    public T[] getLevelOrder() {
        return getBFS();
    }

    /**
     * Get an array representation of the tree in-order.
     * 
     * @param order of search
     * 
     * @return order sorted array representing the tree.
     */
    public T[] getDFS(DepthFirstSearchOrder order) {
        return getDFS(order, this.root, this.size);
    }

    /**
     * Get an array representation of the tree in-order.
     * 
     * @param order of search
     * @param start rooted node
     * @param size of tree rooted at start
     * 
     * @return order sorted array representing the tree.
     */
    public static <T extends Comparable<T>> T[] getDFS(DepthFirstSearchOrder order, Node<T> start, int size) {
        final Set<Node<T>> added = new HashSet<Node<T>>(2);
        final T[] nodes = (T[])Array.newInstance(start.id.getClass(), size);
        int index = 0;
        Node<T> node = start;
        while (index < size && node != null) {
            Node<T> parent = node.getParent();
            Node<T> lesser = (node.getLesser() != null && !added.contains(node.getLesser())) ? node.getLesser() : null;
            Node<T> greater = (node.greater != null && !added.contains(node.greater)) ? node.greater : null;

            if (parent == null && lesser == null && greater == null) {
                if (!added.contains(node))
                    nodes[index++] = node.id;
                break;
            }

            if (order == DepthFirstSearchOrder.inOrder) {
                if (lesser != null) {
                    node = lesser;
                } else {
                    if (!added.contains(node)) {
                        nodes[index++] = node.id;
                        added.add(node);
                    }
                    if (greater != null) {
                        node = greater;
                    } else if (added.contains(node)) {
                        node = parent;
                    } else {
                        // We should not get here. Stop the loop!
                        node = null;
                    }
                }
            } else if (order == DepthFirstSearchOrder.preOrder) {
                if (!added.contains(node)) {
                    nodes[index++] = node.id;
                    added.add(node);
                }
                if (lesser != null) {
                    node = lesser;
                } else if (greater != null) {
                    node = greater;
                } else if (added.contains(node)) {
                    node = parent;
                } else {
                    // We should not get here. Stop the loop!
                    node = null;
                }
            } else {
                // post-Order
                if (lesser != null) {
                    node = lesser;
                } else {
                    if (greater != null) {
                        node = greater;
                    } else {
                        // lesser==null && greater==null
                        nodes[index++] = node.id;
                        added.add(node);
                        node = parent;
                    }
                }
            }
        }
        return nodes;
    }

    /**
     * Get an array representation of the tree in sorted order.
     * 
     * @return sorted array representing the tree.
     */
    public T[] getSorted() {
        // Depth first search to traverse the tree in-order sorted.
        return getDFS(DepthFirstSearchOrder.inOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.Collection<T> toCollection() {
        return (new JavaCompatibleBinarySearchTree<T>(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }

    public static class Node<T extends Comparable<T>> {

        protected T id = null;
//        protected Node<T> parent = null;
        protected Node<T> parent_ = null;
        protected Node<T> lesser_ = null;
        protected Node<T> greater = null;

        /**
         * Node constructor.
         * 
         * @param parent
         *            Parent link in tree. parent can be NULL.
         * @param id
         *            T representing the node in the tree.
         */
        public Node(Node<T> parent, T id) {
            this.parent_ = parent;
            this.id = id;
        }

        protected void setParent(Node<T> p) {
            log.info("setParent");
            parent_ = p;
        }
        protected Node<T> getParent() {
            return parent_;
        }
        protected void setLesser(Node<T> p) {
            log.info("setLesser");
            lesser_ = p;
        }
        protected Node<T> getLesser() {
            return lesser_;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "id=" + id + " parent=" + ((parent_ != null) ? parent_.id : "NULL") + " lesser="
                    + ((lesser_ != null) ? lesser_.id : "NULL") + " greater=" + ((greater != null) ? greater.id : "NULL");
        }
    }

    public static interface INodeCreator<T extends Comparable<T>> {

        /**
         * Create a new Node with the following parameters.
         * 
         * @param parent
         *            of this node.
         * @param id
         *            of this node.
         * @return new Node
         */
        public Node<T> createNewNode(Node<T> parent, T id);
    }

    protected static class TreePrinter {

        public static <T extends Comparable<T>> String getString(BinarySearchTree<T> tree) {
            if (tree.root == null)
                return "Tree has no nodes.";
            return getString(tree.root, "", true);
        }

        private static <T extends Comparable<T>> String getString(Node<T> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            if (node.getParent() != null) {
                String side = "left";
                if (node.equals(node.getParent().greater))
                    side = "right";
                builder.append(prefix + (isTail ? "└── " : "├── ") + "(" + side + ") " + node.id + "\n");
            } else {
                builder.append(prefix + (isTail ? "└── " : "├── ") + node.id + "\n");
            }
            List<Node<T>> children = null;
            if (node.getLesser() != null || node.greater != null) {
                children = new ArrayList<Node<T>>(2);
                if (node.getLesser() != null)
                    children.add(node.getLesser());
                if (node.greater != null)
                    children.add(node.greater);
            }
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    builder.append(getString(children.get(i), prefix + (isTail ? "    " : "│   "), false));
                }
                if (children.size() >= 1) {
                    builder.append(getString(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }

    private static class JavaCompatibleBinarySearchTree<T extends Comparable<T>> extends java.util.AbstractCollection<T> {

        protected BinarySearchTree<T> tree = null;

        public JavaCompatibleBinarySearchTree(BinarySearchTree<T> tree) {
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
            return (tree.remove((T)value)!=null);
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
        public java.util.Iterator<T> iterator() {
            return (new BinarySearchTreeIterator<T>(this.tree));
        }

        private static class BinarySearchTreeIterator<C extends Comparable<C>> implements java.util.Iterator<C> {

            private BinarySearchTree<C> tree = null;
            private BinarySearchTree.Node<C> last = null;
            private Deque<BinarySearchTree.Node<C>> toVisit = new ArrayDeque<BinarySearchTree.Node<C>>();

            protected BinarySearchTreeIterator(BinarySearchTree<C> tree) {
                this.tree = tree;
                if (tree.root!=null) toVisit.add(tree.root);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                if (toVisit.size()>0) return true; 
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public C next() {
                while (toVisit.size()>0) {
                    // Go thru the current nodes
                    BinarySearchTree.Node<C> n = toVisit.pop();

                    // Add non-null children
                    if (n.getLesser()!=null) toVisit.add(n.getLesser());
                    if (n.greater!=null) toVisit.add(n.greater);

                    // Update last node (used in remove method)
                    last = n;
                    return n.id;
                }
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                tree.removeNode(last);
            }
        }
    }
}
