// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev;

/** Modified from java-algorithms-implementation
 *  at https://code.google.com/archive/p/java-algorithms-implementation/
 *  released under Apache license 2.0
 */
/**
 * A tree can be defined recursively (locally) as a collection of nodes (starting at a root node),
 * where each node is a data structure consisting of a value, together with a list of nodes (the "children"),
 * with the constraints that no node is duplicated. A tree can be defined abstractly as a whole (globally)
 * as an ordered tree, with a value assigned to each node.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">Tree (Wikipedia)</a>
 * <br>
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public interface ITree<K, V> {

    /**
     * Add value to the tree. Tree can contain multiple equal values.
     *
     * @param value to put to the tree.
     * @return True if successfully added to tree.
     */
    public boolean put(K key, V value);

    /**
     * Remove first occurrence of value in the tree.
     *
     * @param value to remove from the tree.
     * @return K value removed from tree.
     */
    public K remove(K value);

    /**
     * Clear the entire stack.
     */
    public void clear();

    /**
     * Does the tree contain the value.
     *
     * @param value to locate in the tree.
     * @return True if tree contains value.
     */
    public boolean contains(K value);

    /**
     * Get number of nodes in the tree.
     *
     * @return Number of nodes in the tree.
     */
    public int size();

    /**
     * Validate the tree according to the invariants.
     *
     * @return True if the tree is valid.
     */
    public boolean validate();

}