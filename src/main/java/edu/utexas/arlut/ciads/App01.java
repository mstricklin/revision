// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newTreeMap;

import java.util.concurrent.ConcurrentSkipListMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App01 {
    public static void main(String[] args) {
        log.info("Hello world!");
        ConcurrentSkipListMap<Integer, String> cslm = new ConcurrentSkipListMap<>();
        for (int i = 0; i < 8; ++i)
            cslm.put(i, "aaa"+i);
        log.info("cslm {}", cslm);
        log.info("cslm {}", cslm.headMap(4));

        cslm.remove(2);
        log.info("cslm {}", cslm);
        log.info("cslm {}", cslm.headMap(4));
        newTreeMap();
    }
}
