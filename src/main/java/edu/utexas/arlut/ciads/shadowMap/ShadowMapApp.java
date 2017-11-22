// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.shadowMap;

import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
public class ShadowMapApp {
    public static void main(String[] args) {
        ShadowMap<String, String> cm = new ShadowMap();
        cm.put("AAA", "AAA1");
        cm.put("AAB", "AAA1");
        cm.put("BBB", "AAA1");
        cm.put("CCC", "AAA1");
        cm.dump();
        log.info("");
        cm.remove("AAA");
        cm.dump();
        log.info("get(\"BBB\") {}", cm.get("BBB"));
        log.info("get(\"AAA\") {}", cm.get("AAA"));
        log.info("get(\"CCC\") {}", cm.get("CCC"));

        log.info("");
        log.info("commit...");
        cm.commit();
        cm.dump();
        log.info("");
        cm.put("CCC", "AAA2");
        log.info("get(\"BBB\") {}", cm.get("BBB"));
        log.info("get(\"AAA\") {}", cm.get("AAA"));
        log.info("get(\"CCC\") {}", cm.get("CCC"));

        cm.dump();
        log.info("");
        log.info("rollback...");
        cm.rollback();
        cm.dump();

        cm.put("ZZZ", "Z1");
        cm.commit();
        log.info("");
        cm.dump();
        cm.put("XXX", "X1");
        log.info("");
        cm.dump();
        cm.rollback();
        log.info("");
        cm.dump();

        log.info("");
        cm.remove("AAB");
        cm.dump();

        log.info("");
        if (cm.containsValue("AAA1"))
            log.info("contains AAA1");

        final Set<String> keys = newHashSet();
        cm.forEach((k, v) -> keys.add(k));


        IntStream.range(0, 10000)
                 .mapToObj(Integer::toString)
                 .forEach(i -> {
                     cm.put("K" + i, "V" + i);
                     cm.put("Ka" + i, "Va" + i);
                     cm.commit();
                 });

        cm.dumpAll();


        for (int i=0; i<500; ++i) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info("K{} {}", i, cm.get("K"+i));
            stopwatch.stop(); // optional
            log.info("time: {}", stopwatch);
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("K999 {}", cm.get("K999"));
        stopwatch.stop(); // optional
        log.info("time: {}", stopwatch);

        stopwatch.reset();
        stopwatch.start();
        log.info("K23 {}", cm.get("K23"));
        stopwatch.stop(); // optional
        log.info("time: {}", stopwatch);



    }

    @ToString
    @RequiredArgsConstructor
    static class Key {
        static int ID = 0;
        final int id = ID++;
        final String name;
    }

    @ToString
    @RequiredArgsConstructor
    static class Value {
        static int ID = 0;
        final int id = ID++;
        final String name;
    }
}
