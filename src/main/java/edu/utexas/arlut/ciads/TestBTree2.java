// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.util.function.Supplier;
import java.util.stream.Stream;

import edu.utexas.arlut.ciads.rev2.BTree2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBTree2 {
    public static void main(String[] args) {
        BTree2<String> bt = new BTree2<>(5);

        Stream<String> ss = Stream.generate(new Supplier<String>() {
            int i = 0;
            @Override
            public String get() {
                return "BBB"+i++;
            }
        });

        ss.limit(14).forEach(s -> {
            bt.put(s);
            System.out.println(bt.toString());
            System.out.println();

        });
    }
}
