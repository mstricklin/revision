// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.jc;

import java.util.Stack;

public class BTree {

    // Each Btree object is a B-tree header.

    // This B-tree is represented as follows: arity is the maximum number
    // of children per node, and root is a link to its root node.

    // Each B-tree node is represented as follows: size contains its size; a
    // subarray elems[0...size-1] contains its elements; and a subarray
    // childs[0...size] contains links to its child nodes. For each element
    // elems[i], childs[i] is a link to its left child, and childs[i+1] is a
    // link to its right child. In a leaf node, all child links are null.

    // Moreover, for every element x in the left subtree of element y:
    //    x.compareTo(y) < 0
    // and for every element z in the right subtree of element y:
    //    z.compareTo(y) > 0.

    private int arity;
    private Node root;

    public BTree(int k) {
        // Construct an empty B-tree of arity k.
        root = null;
        arity = k;
    }

    public Node search(Comparable target) {
        // Find which if any node of this B-tree contains an element equal to target.
        // Return a link to that node, or null if there is no such node.
        if (root == null)
            return null;
        Node curr = root;
        for (; ; ) {
            int pos = curr.searchInNode(target);
            if (target.equals(curr.elems[pos]))
                return curr;
            else if (curr.isLeaf())
                return null;
            else
                curr = curr.childs[pos];
        }
    }

    public void put(Comparable k, Object v) {
        insert(k);
    }

    public void insert(Comparable elem) {
        // Insert element elem into this B-tree.
        if (root == null) {
            root = new Node(arity, elem, null, null);
            return;
        }
        Stack ancestors = new Stack();
        Node curr = root;
        for (; ; ) {
            int currPos = curr.searchInNode(elem);
            if (elem.equals(curr.elems[currPos]))
                return;
            else if (curr.isLeaf()) {
                curr.insertInNode(elem, null, null, currPos);
                if (curr.size == arity)  // curr has overflowed
                    splitNode(curr, ancestors);
                return;
            } else {
                ancestors.push(new Integer(currPos));
                ancestors.push(curr);
                curr = curr.childs[currPos];
            }
        }
    }

    private void splitNode(Node node,
                           Stack ancestors) {
        // Split the overflowed node in this B-tree. The stack ancestors contains
        // all ancestors of node, together with the known insertion position in each of
        // these ancestors.
        int medPos = node.size / 2;
        Comparable med = node.elems[medPos];
        Node leftSib = new Node(arity,
                                node.elems, node.childs, 0, medPos);
        Node rightSib = new Node(arity,
                                 node.elems, node.childs, medPos + 1, node.size);
        if (node == root)
            root = new Node(arity, med, leftSib,
                            rightSib);
        else {
            Node parent =
                    (Node)ancestors.pop();
            int parentIns = ((Integer)
                    ancestors.pop()).intValue();
            parent.insertInNode(med, leftSib, rightSib,
                                parentIns);
            if (parent.size == arity)  // parent has overflowed
                splitNode(parent, ancestors);
        }
    }

    public void delete(Comparable elem) {
        // Delete element elem from this B-tree.
        if (root == null)
            return;
        Stack ancestors = new Stack();
        Node curr = root;
        int currPos;
        for (; ; ) {
            currPos = curr.searchInNode(elem);
            if (elem.equals(curr.elems[currPos]))
                break;
            else if (curr.isLeaf())
                return;
            else {
                ancestors.push(new Integer(currPos));
                ancestors.push(curr);
                curr = curr.childs[currPos];
            }
        }
        if (curr.isLeaf()) {
            curr.removeFromNode(currPos, currPos);
            if (underflowed(curr))
                restock(curr, ancestors);
        } else {
            Node leftmostNode = findLeftmostNode(curr.childs[currPos + 1], ancestors);
            Comparable nextElem = leftmostNode.elems[0];
            leftmostNode.removeFromNode(0, 0);
            curr.elems[currPos] = nextElem;
            if (underflowed(leftmostNode))
                restock(leftmostNode, ancestors);
        }
    }

    private void restock(Node node, Stack ancestors) {
        // Restock node, which has underflowed.
        // The stack ancestors contains all ancestors of node, together
        // with the child position in each of these ancestors.
        if (node == root) {  // node.size == 0
            root = node.childs[0];
            return;
        }
        Node parent = (Node)ancestors.pop();
        int childPos = 0;
        while (parent.childs[childPos] != node) childPos++;
        int sibMinSize = (arity - 1) / 2;
        if (childPos > 0 && parent.childs[childPos - 1].size > sibMinSize) {
            Node sib = parent.childs[childPos - 1];
            Comparable parentElem = parent.elems[childPos - 1];
            Comparable spareElem = sib.elems[sib.size - 1];
            Node spareChild = sib.childs[sib.size];
            sib.removeFromNode(sib.size - 1, sib.size);
            node.insertInNode(parentElem, spareChild, node.childs[0], 0);
            parent.elems[childPos - 1] = spareElem;
        } else if (childPos < parent.size && parent.childs[childPos + 1].size > sibMinSize) {
            Node sib = parent.childs[childPos + 1];
            Comparable parentElem = parent.elems[childPos];
            Comparable spareElem = sib.elems[0];
            Node spareChild = sib.childs[0];
            sib.removeFromNode(0, 0);
            node.insertInNode(parentElem, node.childs[node.size], spareChild, node.size);
            parent.elems[childPos] = spareElem;
        } else if (childPos > 0) {
            Node sib = parent.childs[childPos - 1];
            Comparable parentElem = parent.elems[childPos - 1];
            node.coalesceLeft(sib, parentElem);
            parent.removeFromNode(childPos - 1, childPos - 1);
            if (underflowed(parent))
                restock(parent, ancestors);
        } else {  // childPos < parent.size
            Node sib = parent.childs[childPos + 1];
            Comparable parentElem = parent.elems[childPos];
            node.coalesceRight(parentElem, sib);
            parent.removeFromNode(childPos, childPos + 1);
            if (underflowed(parent))
                restock(parent, ancestors);
        }
    }

    private void removeFromNode(Node node, int elemPos, int childPos) {  // OBSOLETE
        // Remove from node the element whose index is elemPos, and the child
        // whose index is childPos.
        for (int i = elemPos; i < node.size; i++)
            node.elems[i] = node.elems[i + 1];
        if (!node.isLeaf()) {
            for (int i = childPos; i < node.size; i++)
                node.childs[i] = node.childs[i + 1];
        }
        node.size--;
    }

    private Node findLeftmostNode(Node top, Stack ancestors) {
        // Return the leftmost leaf node in the subtree whose topmost node is top.
        // Push the node's ancestors on to the stack ancestors.
        Node curr = top;
        while (!curr.isLeaf()) {
            ancestors.push(new Integer(0));
            ancestors.push(curr);
            curr = curr.childs[0];
        }
        return curr;
    }

    private boolean underflowed(Node node) {
        // Return true if and only if node has underflowed.
        int minSize = (node == root ? 1 : (arity - 1) / 2);
        return (node.size < minSize);
    }

    //////////// Driver ////////////

    public void print() {
        // Print a textual representation of this B-tree.
        printSubtree(root, "");
    }

    private static void printSubtree(Node top, String indent) {
        // Print a textual representation of the subtree of this B-tree whose
        // topmost node is top, indented by the string of spaces indent.
        if (top == null)
            ; //System.out.println(indent + "o");
        else {
//            System.out.println(indent + "o-->");
            boolean isLeaf = top.isLeaf();
            String childIndent = indent + "    ";
            for (int i = 0; i < top.size; i++) {
                if (!isLeaf)
                    printSubtree(top.childs[i], childIndent);
                System.out.println(childIndent + top.elems[i]);
            }
            if (!isLeaf)
                printSubtree(top.childs[top.size], childIndent);
        }
    }

    public static void main(String[] args) {
        BTree bt = new BTree(5);
        boolean deleting = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("ins")) deleting = false;
            else if (arg.equals("del")) deleting = true;
            else if (arg.equals("pri")) bt.print();
            else {
                if (deleting) {
                    System.out.println("Deleting " + arg);
                    bt.delete(arg);
                } else {
                    System.out.println("Inserting " + arg);
                    bt.insert(arg);
                }
            }
        }
    }

    //////////// Inner class ////////////

    public static class Node {

        // Each Node object is a B-tree node.

        private int size;
        private Comparable[] elems;
        private Node[] childs;

        private Node(int k, Comparable elem,
                     Node left, Node right) {
            // Construct a B-tree node of arity k, initially with one element, elem,
            // and two children, left and right.
            elems = new Comparable[k];
            childs = new Node[k + 1];
            // ... Each array has one extra component, to allow for possible
            // overflow.
            this.size = 1;
            this.elems[0] = elem;
            this.childs[0] = left;
            this.childs[1] = right;
        }

        private Node(int k, Comparable[] elems,
                     Node[] childs, int l, int r) {
            // Construct a B-tree node of arity k, with its elements taken from the
            // subarray elems[l...r-1] and its children from the subarray
            // childs[l...r].
            this.elems = new Comparable[k];
            this.childs = new Node[k + 1];
            this.size = 0;
            for (int j = l; j < r; j++) {
                this.elems[this.size] = elems[j];
                this.childs[this.size] = childs[j];
                this.size++;
            }
            this.childs[this.size] = childs[r];
        }

        private boolean isLeaf() {
            // Return true if and only if this node is a leaf.
            return (childs[0] == null);
        }

        private int searchInNode(Comparable target) {
            // Return the index of the leftmost element in this node that is not less
            // than target.
            int l = 0, r = size - 1;
            while (l <= r) {
                int m = (l + r) / 2;
                int comp = target.compareTo(elems[m]);
                if (comp == 0)
                    return m;
                else if (comp < 0)
                    r = m - 1;
                else
                    l = m + 1;
            }
            return l;
        }

        private void insertInNode(Comparable elem,
                                  Node leftChild,
                                  Node rightChild,
                                  int ins) {
            // Insert element elem, with children leftChild and rightChild, at
            // position ins in this node.
            for (int i = size; i > ins; i--) {
                elems[i] = elems[i - 1];
                childs[i + 1] = childs[i];
            }
            size++;
            elems[ins] = elem;
            childs[ins] = leftChild;
            childs[ins + 1] = rightChild;
        }

        private void coalesceLeft(Node that, Comparable elem) {
            // Insert all that node's elements and children, followed by elem,
            // as the leftmost elements and children of this node.
            System.arraycopy(this.elems, 0, this.elems, that.size + 1, this.size);
            System.arraycopy(this.childs, 0, this.childs, that.size + 1, this.size + 1);
            System.arraycopy(that.elems, 0, this.elems, 0, that.size);
            this.elems[that.size] = elem;
            System.arraycopy(that.childs, 0, this.childs, 0, that.size + 1);
            this.size += that.size + 1;
        }

        private void coalesceRight(Comparable elem, Node that) {
            // Insert all that node's elements and children, preceded by elem,
            // as the rightmost elements and children of this node.
            this.elems[this.size] = elem;
            System.arraycopy(that.elems, 0, this.elems, this.size + 1, that.size);
            System.arraycopy(that.childs, 0, this.childs, this.size + 1, that.size + 1);
            this.size += that.size + 1;
        }

        private void removeFromNode(int elemPos, int childPos) {
            // Remove from this node the element at position elemPos, and the child
            // at position childPos.
            for (int i = elemPos; i < size; i++)
                elems[i] = elems[i + 1];
            if (!isLeaf()) {
                for (int i = childPos; i < size; i++)
                    childs[i] = childs[i + 1];
            }
            size--;
        }

    }

}