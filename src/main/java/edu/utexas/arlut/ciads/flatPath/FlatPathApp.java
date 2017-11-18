// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.flatPath;

import java.util.stream.IntStream;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlatPathApp {
    public static void main(String[] args) {

        BloomFilter<Integer> sb = BloomFilter.create(Funnels.integerFunnel(), 500, 0.01);

        IntStream.range(0, 20)
                 .mapToObj(Integer::toString)
                 .map(s -> "BBB"+s)
                 .forEach(s -> {
                     sb.put(s.hashCode());
                 });

        IntStream.range(0, 20)
                 .mapToObj(Integer::toString)
                 .map(s -> "BBB"+s)
                 .forEach(s -> {
                     log.info("{} {}", s, sb.mightContain(s.hashCode()));
                 });


        String ones = "1111111111111111111111111111111";
        log.info("ones len: {}", ones.length());
        int z = Integer.parseInt("1010101010101010", 2);
        log.info("z: {} {}", z, Integer.toBinaryString(z));
        int key = Integer.MAX_VALUE; //43690;
        log.info("{} {}", key, Integer.toBinaryString(key));
        int slide = key;
        for (int i=0; i<8; i++) {
            int bite = slide & 3;
            log.info("{} {} {}", Integer.toBinaryString(key), Integer.toBinaryString(slide), Integer.toBinaryString(bite));
            slide = slide >>> 2;
        }
    }
}
