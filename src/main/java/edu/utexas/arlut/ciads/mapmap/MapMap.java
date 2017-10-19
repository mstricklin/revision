// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.mapmap;

import static com.google.common.collect.Maps.newHashMap;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapMap<K extends Comparable<K>, V> {

    public MapMap(int order) {
        log.info("order: {}", Integer.toBinaryString(order));
        this.order = Integer.highestOneBit(order-1) << 1;
        log.info("Using {}", this.order);
        log.info("1 << 4 {}", 1 << 4);
        log.info("highest 1 bit {}", Integer.highestOneBit(order));

        if (0 != (order & (order - 1))) {
            log.error("order of storage should be a power of 2");
            log.error("Using next higher power of 2:");
        }
//        this.order = order;
        this.mask = order-1;
        log.info("order {}", order);
    }
    public void add(K k, V v) {
        int hash = k.hashCode();
//        hash = 6;
        Integer major = hash & ~mask;
        Integer minor = hash & mask;
        log.info("Major {} {}", major, Integer.toBinaryString(major));
        log.info("Minor {} {}", minor, Integer.toBinaryString(minor));

        Map<K, V> minormap = majormap.computeIfAbsent(major, (key -> newHashMap()));
        minormap.put(k, v);
        log.info("minormap: {}", minormap);

    }
    public void dump() {
        for (Map.Entry<Integer, Map<K,V>> e: majormap.entrySet()) {
            log.info("major: {}", e.getKey());
            log.info("\t{}", e.getValue());
        }
    }

    // =================================
    private final int order;
    private final int mask;

    private final Map<Integer, Map<K, V>> majormap = newHashMap();
}
