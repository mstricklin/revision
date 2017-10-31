package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Map;
import java.util.Set;

import edu.utexas.arlut.ciads.binaryTree.AVLTree;
import edu.utexas.arlut.ciads.binaryTree.BinarySearchTree;
import edu.utexas.arlut.ciads.rev.BTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        BTree<Integer, String> bt = new BTree<>(3);

        String s = new String("foo");
        bt.put(Integer.valueOf(4), "bbb"+Integer.valueOf(4));
        bt.put(Integer.valueOf(3), "bbb"+Integer.valueOf(3));
        bt.put(Integer.valueOf(2), "bbb"+Integer.valueOf(2));

        for (int i = 0; i < 16; i++) {
            System.out.println(bt.toString());
            bt.put(Integer.valueOf(i), "aaa"+Integer.valueOf(i));
        }
        Map<String, String> m = newTreeMap();
        System.out.println(bt.toString());
        bt.put(Integer.valueOf(1), "zzz");
        System.out.println(bt.toString());

        bt.remove(14);
        System.out.println(bt.toString());

        Set<String> ss = newHashSet();


        bt.stream().forEach(e -> log.info("{}", e));


        log.info("====================");
        BinarySearchTree binT = new MyAVLTree(new NodeCreator());
        for (int i = 0; i < 17; i++) {
            System.out.println(binT.toString());
            binT.add(Integer.toString(i));
        }
        System.out.println(binT.toString());
    }
    // =================================
    public static class MyAVLTree extends AVLTree<String> {
        public MyAVLTree(INodeCreator<String> creator) {
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
            return (dirty?"X":"") + id;
//            + " height=" + height + " parent=" + ((parent != null) ? parent.id : "NULL")
//                    + " lesser=" + ((lesser != null) ? lesser.id : "NULL") + " greater="
//                    + ((greater != null) ? greater.id : "NULL");
        }
    }
}

//        bt.put("aaa", "aaa one");
//                bt.put("bbb", "bbb one");
//                bt.put("ccc", "ccc one");
//                bt.put("ddd", "ddd one");
//                bt.put("eee", "eee one");
//                bt.put("fff", "fff one");
//                bt.put("ggg", "ggg one");
//                bt.put("hhh", "hhh one");
//                bt.put("iii", "iii one");
//                bt.put("jjj", "jjj one");
//                bt.put("kkk", "kkk one");
//                bt.put("lll", "lll one");
//                bt.put("mmm", "mmm one");
//                bt.put("nnn", "nnn one");
//                bt.put("ooo", "ooo one");
//                bt.put("ppp", "ppp one");
//                bt.put("qqq", "qqq one");
//                bt.put("rrr", "rrr one");
//                bt.put("sss", "sss one");
//                bt.put("ttt", "ttt one");
//                bt.put("uuu", "uuu one");
//                bt.put("vvv", "vvv one");
//                bt.put("www", "www one");
//                bt.put("xxx", "xxx one");
//                bt.put("yyy", "yyy one");
//                bt.put("zzz", "zzz one");
