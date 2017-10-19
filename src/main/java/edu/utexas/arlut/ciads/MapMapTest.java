// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.mapmap.MapMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapMapTest {
    public static void main(String[] args) {
        MapMap<String, String> mm = new MapMap<>(64);

        mm.add("AAA", "AAA1");
        mm.add("AAB", "AAA1");
        mm.add("BBB", "AAA1");
        mm.add("CCC", "AAA1");

        mm.dump();


    }
}
