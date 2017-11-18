// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.shadowMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class TestShadowMap2 {
    private ShadowMap<String, String> cm;
    @Before
    public void before() {
        cm = new ShadowMap();
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
}
