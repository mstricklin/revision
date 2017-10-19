// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.rev2.BTree2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBTree2 {
    public static void main(String[] args) {
        BTree2<String> bt = new BTree2<>(16);
        bt.add("Foo");
        bt.add("Bar");
        bt.add("Baz");
        System.out.println(bt.toString());
    }
}
