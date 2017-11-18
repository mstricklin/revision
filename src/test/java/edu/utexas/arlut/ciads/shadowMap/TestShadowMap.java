// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.shadowMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.arlut.ciads.shadowMap.ShadowMap;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;

@Slf4j
public class TestShadowMap {
    private ShadowMap<String, String> cm;
    @Before
    public void before() {
        cm = new ShadowMap();
    }
    // =================================
    @Test
    public void containsKey() {
        cm.put("K1", "V1");
        assertTrue(cm.containsKey("K1"));
        cm.commit();
        assertTrue(cm.containsKey("K1"));
        assertFalse(cm.containsKey("K2"));

        cm.put("K3", "V3");
        assertTrue(cm.containsKey("K3"));
        cm.rollback();
        assertFalse(cm.containsKey("K3"));
    }
    // =================================
    @Test
    public void remove() {
        cm.put("K1", "V1");
        assertTrue(cm.containsKey("K1"));
        cm.commit();
        assertTrue(cm.containsKey("K1"));
        assertFalse(cm.isEmpty());
        cm.remove("K1");
        assertFalse(cm.containsKey("K1"));
        cm.commit();
        assertFalse(cm.containsKey("K1"));
        assertTrue(cm.isEmpty());

        cm.put("K2", "V2");
        assertTrue(cm.containsKey("K2"));
        cm.commit();
        assertFalse(cm.isEmpty());
        cm.remove("K2");
        assertTrue(cm.isEmpty());
        assertFalse(cm.containsKey("K2"));
        cm.rollback();
        assertTrue(cm.containsKey("K2"));
        assertFalse(cm.isEmpty());
    }
    // =================================
    @Test
    public void get() {
        cm.put("K1", "V1");
        assertTrue(cm.containsKey("K1"));
        assertEquals("V1", cm.get("K1"));
        cm.rollback();
        assertNull(cm.get("K1"));

        cm.put("K2", "V2");
        assertEquals("V2", cm.get("K2"));
        cm.commit();
        assertEquals("V2", cm.get("K2"));
        cm.put("K2", "V2a");
        assertEquals("V2a", cm.get("K2"));
        cm.rollback();
        assertEquals("V2", cm.get("K2"));

        cm.remove("K2");
        assertNull(cm.get("K2"));
    }
    // =================================
    @Test
    public void put() {
        cm.put("K1", "V1");
        assertTrue(cm.containsKey("K1"));
        assertEquals("V1", cm.get("K1"));
        cm.rollback();
        assertNull(cm.get("K1"));
        assertEquals(0, cm.size());
        assertTrue(cm.isEmpty());

        cm.put("K2", "V2");
        assertEquals("V2", cm.get("K2"));
        assertEquals(1, cm.size());
        assertFalse(cm.isEmpty());
        cm.commit();
        assertEquals("V2", cm.get("K2"));
        assertEquals(1, cm.size());
        assertFalse(cm.isEmpty());
    }
    // =================================
    @Test
    public void size() {
        // changes size:
        // 1. put
        // 2. remove
        // 3. putAll
        // 4. clear
        // 5. rollback
        assertEquals(0, cm.size());
        cm.put("K1", "V1a");
        assertEquals(1, cm.size());
        cm.put("K1", "V1b");
        assertEquals(1, cm.size());
        cm.put("K2", "V2");
        assertEquals(2, cm.size());
        cm.rollback();
        assertEquals(0, cm.size());

        cm.put("K3", "V3");
        cm.commit();
        assertEquals(1, cm.size());
        cm.put("DDD", "V4");
        assertEquals(2, cm.size());
        cm.rollback();
        assertEquals(1, cm.size());

        cm.remove("K3");
        assertEquals(0, cm.size());
        cm.rollback();
        assertEquals(1, cm.size());

        // putAll
        // clear
    }
    // =================================
    @Test
    public void containsValue() {
        cm.put("K1", "V1");
        cm.put("K2", "V2");
        assertTrue(cm.containsValue("V1"));
        assertFalse(cm.containsValue("V99"));

        cm.commit();
        cm.put("K3", "V3");
        assertTrue(cm.containsValue("V1"));
        assertTrue(cm.containsValue("V3"));
        assertFalse(cm.containsValue("V99"));

        cm.rollback();
        assertFalse(cm.containsValue("V3"));

        cm.put("K3", "V3");
        assertTrue(cm.containsValue("V3"));
        cm.commit();
        assertTrue(cm.containsValue("V3"));
        cm.remove("K3");
        assertFalse(cm.containsValue("V3"));
        cm.rollback();
        assertTrue(cm.containsValue("V3"));
    }
    // =================================
    @Test
    public void entrySet() {
        cm.put("K1", "V1");
        cm.put("K2", "V2");
        for (Map.Entry<String,String> e: cm.entrySet()) {
            log.info("{} => {}", e.getKey(), e.getValue());
        }

        // clear
        // remove
        // size
        // iterator
        // contains
    }
    // =================================
    @Test
    public void keySet() {
        cm.put("K1", "V1");
        cm.put("K2", "V2");
        cm.commit();
        cm.put("K3", "V3");

        Set<String> keys = cm.keySet();
        assertThat(keys, hasItems("K1", "K2", "K3"));
        assertEquals(3, keys.size());
        assertFalse(keys.contains("K4"));

        cm.rollback();
        assertEquals(keys, newHashSet("K1", "K2"));
        assertFalse(keys.contains("K3"));

        cm.remove("K2");
        keys = cm.keySet();
        assertThat(keys, hasItems("K1"));
        assertEquals(1, keys.size());
        assertFalse(keys.contains("K2"));

        // clear
        // remove
        // size
        // iterator
        // contains
    }
    // =================================
    @Test
    public void clear() {
        cm.put("K1", "V1");
        cm.put("K2", "V2");
        cm.commit();
        cm.put("K3", "V3");
        cm.remove("K2");
        cm.dump();
        for (String k: cm.keySet())
            log.info("key: {}", k);
        log.info("");
        cm.clear();

        cm.dump();
        log.info("");
        cm.dumpAll();
        for (String k: cm.keySet())
            log.info("key: {}", k);
        log.info("");
        cm.put("K1", "V1");
        cm.dump();
        log.info("");
        cm.dumpAll();
        log.info("");
        for (String k: cm.keySet())
            log.info("key: {}", k);

    }
    // =================================
    @Test
    public void forEach() {
        cm.put("K1", "V1");
        cm.dumpAll();
        cm.commit();
        log.info("");

        cm.put("K2", "V3");
        cm.dumpAll();
        cm.commit();
        log.info("");

        cm.clear();
        cm.put("K3", "V3");
        cm.dumpAll();
        cm.commit();
        log.info("");

        cm.put("K4", "V4");
        cm.dumpAll();
        cm.commit();
        log.info("");

        final Set<String> keys = newHashSet();
        cm.forEach((k,v) -> keys.add(k));
//        final Set<String> keys = newHashSet();
//        cm.put("K1", "V1");
//        cm.put("K2", "V2");
//        cm.forEach((k,v) -> keys.add(k));
//        assertEquals(keys, newHashSet("K1", "K2"));
//
//        cm.commit();
//        cm.remove("K1");
//        keys.clear();
//        cm.dumpAll();
//        cm.forEach((k,v) -> log.info("{} => {}", k, v));
//        cm.forEach((k,v) -> keys.add(k));
////        assertEquals(keys, newHashSet("K2"));
//
//        log.info("");
//        for (Map.Entry e: cm.entrySet())
//            log.info("{} => {}", e.getKey(), e.getValue());
//
    }
}
