// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.jwetherell.algorithms.data_structures.BTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestBTree {
    public static void main(String[] args) {
        System.out.printf("Hello world!\n");
        BTree<String> bt = new BTree<>();
        IntStream is = IntStream.range(0, 10);

        Stream<String> ss = Stream.generate(new Supplier<String>() {
            int i = 0;
            @Override
            public String get() {
                return "BBB"+i++;
            }
        });

        ss.limit(3).forEach(s -> {
            bt.add(s);
            System.out.println(bt.toString());

        });


        Stream.generate(new StringSupplier())
              .limit(3).forEach(s -> {
            bt.add(s);
            System.out.println(bt.toString());

        });

    }

    private static class StringSupplier implements Supplier<String> {
        int i = 0;

        @Override
        public String get() {
            return "AAA" + i++;
        }
    }
}
