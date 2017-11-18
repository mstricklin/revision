// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev2;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BTreeSansParentApp {
    public static void main(String[] args) {
        System.out.printf("Hello world!\n");

        BTreeSansParent<String> bt = new BTreeSansParent();

//        ss.limit(19).forEach(s -> {
//            bt.put(s);
//            System.out.println(bt.toString());
//            System.out.println();
//
//        });

        IntStream.range(0, 20)
                 .mapToObj(Integer::toString)
                 .map(s -> "BBB"+s)
                 .forEach(s -> {
                     bt.add(s);
                     System.out.println(bt.toString());
                     System.out.println();

                 });


    }
}
