// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Lists.newArrayList;

import edu.utexas.arlut.ciads.binaryTree.BinaryTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryTreeApp {
    public static void main(String[] args) {
        BinaryTree<String, String> bt = new BinaryTree<>();
//        for (Integer i : newArrayList(1, 2, 3, 4, 5, 6, 7, 8)) {
        for (Integer i : newArrayList(4, 2, 6, 1, 3, 5, 7, 8)) {
            System.out.printf("== add %d ============================\n", i);
            bt.add(i + "key", i+"val");
            System.out.println(bt.toString());
            bt.commit();
        }
//        bt.add("4key", 4);
//
//        bt.add("2key", 2);
//        bt.add("6key", 6);
//
//        bt.add("1key", 1);
//        bt.add("3key", 3);
//        bt.add("5key", 5);
//        bt.add("7key", 7);
//
//        bt.add("8key", 8);

        System.out.println(bt.toString());
//        System.out.printf("========================================\n");
//        bt.allTime();
//
//        System.out.printf("Contains 2key %b\n", bt.contains("2key"));
//        System.out.printf("Contains 0key %b\n", bt.contains("0key"));
//        System.out.printf("Contains 4key %b\n", bt.contains("4key"));
//        System.out.printf("get 4key %s\n", bt.get("4key"));

    }
}
