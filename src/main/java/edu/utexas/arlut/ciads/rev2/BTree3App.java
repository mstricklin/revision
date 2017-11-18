// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev2;

import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BTree3App {
    public static void main(String[] args) {

        // for splay 6
//        float splay = 2.5f;
//        for (int i=1; i<12; i++) {
//            int cnt = (int)Math.pow(splay, i);
//            log.info("depth {} count {} {}", i, cnt, Math.pow(splay, i));
//        }


        double depth = Math.log(16000) / Math.log(6);
        log.info("depth: {}", depth);
        log.info("depth: {}", Math.ceil(depth));

        BTree3<String> bt = new BTree3<>(5);

        Stream<String> ss = Stream.generate(new Supplier<String>() {
            int i = 0;
            @Override
            public String get() {
                return "BBB"+i++;
            }
        });

        ss.limit(19).forEach(s -> {
            bt.put(s);
            System.out.println(bt.toString());
            System.out.println();

        });

    }
}
