// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.binaryTree.AVLTree;
import edu.utexas.arlut.ciads.binaryTree.BinarySearchTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinTreeApp {
    public static void main(String[] args) {

        BinarySearchTree binT = new MyAVLTree(new NodeCreator());
        for (int i = 0; i < 17; i++) {
            System.out.println(binT.toString());
            binT.add(Integer.toString(i));
        }
        System.out.println(binT.toString());
    }

    // =================================
    public static class MyAVLTree extends AVLTree<String> {
        public MyAVLTree(BinarySearchTree.INodeCreator<String> creator) {
            super((Node<String> parent, String id) -> new MyAVLNode(parent, id));
        }
    }
    public static class NodeCreator implements BinarySearchTree.INodeCreator<String> {

        @Override
        public BinarySearchTree.Node createNewNode(BinarySearchTree.Node parent, String id) {
            return new MyAVLNode(parent, id);
        }
    }
    public static class MyAVLNode extends AVLTree.AVLNode<String> {
        protected boolean dirty=true;

        public MyAVLNode(BinarySearchTree.Node parent, String value) {
            super(parent, value);
        }
        @Override
        public String toString() {
            return (dirty?"X-":"") + id;
//            + " height=" + height + " parent=" + ((parent != null) ? parent.id : "NULL")
//                    + " lesser=" + ((lesser != null) ? lesser.id : "NULL") + " greater="
//                    + ((greater != null) ? greater.id : "NULL");
        }
        protected void setParent(BinarySearchTree.Node<String> p) {
            log.info("setParent");
            this.parent_ = p;
        }
        protected BinarySearchTree.Node<String> getParent() {
            return this.parent_;
        }
        protected void setLesser(BinarySearchTree.Node<String> p) {
            log.info("setLesser");
            this.lesser_ = p;
        }
        protected BinarySearchTree.Node<String> getLesser() {
            return this.lesser_;
        }
    }
}
