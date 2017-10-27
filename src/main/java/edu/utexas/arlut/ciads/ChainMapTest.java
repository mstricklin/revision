// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import edu.utexas.arlut.ciads.chain.ChainMap;
import edu.utexas.arlut.ciads.chain.ChainMap2;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
public class ChainMapTest {
    public static void main(String[] args) {
        ChainMap2<String, String> cm = new ChainMap2();
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

        newHashMap();

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
