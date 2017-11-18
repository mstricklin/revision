// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.flatPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlatPath {

    public void add(String item) {
        final int hash = item.hashCode();

    }
    private interface Node {
        boolean isLeaf();
    }
    private static class Interior implements Node {

        @Override
        public boolean isLeaf() {
            return false;
        }
        private Node[] children = new Node[4];
    }
    private static class Leaf implements Node {

        @Override
        public boolean isLeaf() {
            return true;
        }
        private Object[] a0 = null;
        private Object[] a1 = null;
        private Object[] a2 = null;
        private Object[] a3 = null;
    }
}
