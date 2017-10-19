// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.chain.ChainMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChainMapTest {
    public static void main(String[] args) {
        ChainMap<String, String> cm = new ChainMap();
        cm.put("AAA", "AAA1");
        cm.put("AAB", "AAA1");
        cm.put("BBB", "AAA1");
        cm.put("CCC", "AAA1");
        log.info("dump {}", cm);
        cm.remove("AAA");
        log.info("dump {}", cm);
        log.info("get(\"BBB\") {}", cm.get("BBB"));
        log.info("get(\"AAA\") {}", cm.get("AAA"));
        log.info("get(\"CCC\") {}", cm.get("CCC"));

        cm.commit();
        log.info("");
        cm.put("CCC", "AAA2");
        log.info("get(\"BBB\") {}", cm.get("BBB"));
        log.info("get(\"AAA\") {}", cm.get("AAA"));
        log.info("get(\"CCC\") {}", cm.get("CCC"));
        log.info("dump {}", cm);

    }
}
