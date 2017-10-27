// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.chain;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;

@Slf4j
public class TestChainMap2 {
    private ChainMap2<String, String> cm;
    @Before
    public void sam() {
        cm = new ChainMap2();
    }
    // =================================
    @Test
    public void containsKey() {
        cm.put("AAA", "one");
        assertTrue(cm.containsKey("AAA"));
        cm.commit();
        assertTrue(cm.containsKey("AAA"));
        assertFalse(cm.containsKey("BBB"));

        cm.put("CCC", "three");
        assertTrue(cm.containsKey("CCC"));
        cm.rollback();
        assertFalse(cm.containsKey("CCC"));
    }
    // =================================
    @Test
    public void remove() {
        cm.put("AAA", "one");
        assertTrue(cm.containsKey("AAA"));
        cm.commit();
        assertTrue(cm.containsKey("AAA"));
        assertFalse(cm.isEmpty());
        cm.remove("AAA");
        assertFalse(cm.containsKey("AAA"));
        cm.commit();
        assertFalse(cm.containsKey("AAA"));
        assertTrue(cm.isEmpty());

        cm.put("BBB", "two");
        assertTrue(cm.containsKey("BBB"));
        cm.commit();
        assertFalse(cm.isEmpty());
        cm.remove("BBB");
        assertTrue(cm.isEmpty());
        assertFalse(cm.containsKey("BBB"));
        cm.rollback();
        assertTrue(cm.containsKey("BBB"));
        assertFalse(cm.isEmpty());
    }
    // =================================
    @Test
    public void get() {
        cm.put("AAA", "one");
        assertTrue(cm.containsKey("AAA"));
        assertEquals("one", cm.get("AAA"));
        cm.rollback();
        assertNull(cm.get("AAA"));

        cm.put("BBB", "two");
        assertEquals("two", cm.get("BBB"));
        cm.commit();
        assertEquals("two", cm.get("BBB"));
        cm.put("BBB", "TWO");
        assertEquals("TWO", cm.get("BBB"));
        cm.rollback();
        assertEquals("two", cm.get("BBB"));
    }
    // =================================
    @Test
    public void put() {
        cm.put("AAA", "one");
        assertTrue(cm.containsKey("AAA"));
        assertEquals("one", cm.get("AAA"));
        cm.rollback();
        assertNull(cm.get("AAA"));
        assertEquals(0, cm.size());
        assertTrue(cm.isEmpty());

        cm.put("BBB", "two");
        assertEquals("two", cm.get("BBB"));
        assertEquals(1, cm.size());
        assertFalse(cm.isEmpty());
        cm.commit();
        assertEquals("two", cm.get("BBB"));
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
        cm.put("AAA", "one");
        assertEquals(1, cm.size());
        cm.put("AAA", "ONE");
        assertEquals(1, cm.size());
        cm.put("BBB", "two");
        assertEquals(2, cm.size());
        cm.rollback();
        assertEquals(0, cm.size());

        cm.put("CCC", "three");
        cm.commit();
        assertEquals(1, cm.size());
        cm.put("DDD", "four");
        assertEquals(2, cm.size());
        cm.rollback();
        assertEquals(1, cm.size());

        cm.remove("CCC");
        assertEquals(0, cm.size());
        cm.rollback();
        assertEquals(1, cm.size());

        // putAll
        // clear
    }
    // =================================
    @Test
    public void allKeys() {
        ChainMap2<String, String> cm = new ChainMap2();
        cm.put("A1", "A1");
        cm.put("A2", "A2");
        cm.commit();
        cm.put("B3", "B3");
        cm.put("B4", "B4");
        cm.commit();
        cm.put("C5", "C5");
        cm.put("C6", "C6");
        cm.put("A1", "C7");
        cm.remove("A2");
        cm.commit();

        cm.allKeys();
    }
}
