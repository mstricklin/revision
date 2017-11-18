// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Lists.newArrayList;

import java.util.UUID;

import edu.utexas.arlut.ciads.binaryTree.BinaryTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryTreeApp {
    public static void main(String[] args) {
        BinaryTree<String, String> bt = new BinaryTree<>();
//        for (Integer id : newArrayList(1, 2, 3, 4, 5, 6, 7, 8)) {
        for (Integer id : newArrayList(4, 2, 6, 1, 3, 5, 7, 8)) {
//            UUID id = UUID.randomUUID();
            System.out.printf("== put %s ============================\n", id);
            bt.add(id.toString()+"key", id+"val");
            System.out.println(bt.toString());
            bt.commit();
            System.out.println("size " + bt.size());
        }
        System.out.printf("== put 1 ============================\n");
        bt.add("1key", "1new-val");
        System.out.println(bt.toString());
        bt.commit();
        System.out.println("size " + bt.size());

//        bt.put("4key", 4);
//
//        bt.put("2key", 2);
//        bt.put("6key", 6);
//
//        bt.put("1key", 1);
//        bt.put("3key", 3);
//        bt.put("5key", 5);
//        bt.put("7key", 7);
//
//        bt.put("8key", 8);

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
