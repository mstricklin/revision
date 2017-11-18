// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.rev;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MTree {
    private static final int order = 4;
    private static final Node root = new Node(order);

    public void put(String value) {
        int hash = value.hashCode();
        int bite = hash;
        log.info("hashCode {}", hash);
        log.info("hashCode binary: {}", Integer.toBinaryString(hash));
        log.info("length: {}", Integer.toBinaryString(hash).length());

        StringBuffer b = new StringBuffer();
        List<Integer> l = newArrayList();
        for (int i=0; i<16; i++) {
            String s = Strings.padStart(Integer.toBinaryString(bite & 3), 2, '0');
            log.info("bite: {}", s);
            b.append(s);
//            l.

            log.info("");
            bite >>= 2;
        }
//        log.info("hashCode binary: {}", b);
        log.info("hashCode binary: {}", Integer.toBinaryString(hash));


        log.info("");
    }


    private static class Node {
        private final Node[] children;
        Node(int sz) {
            children = new Node[sz];
        }
    }
}
