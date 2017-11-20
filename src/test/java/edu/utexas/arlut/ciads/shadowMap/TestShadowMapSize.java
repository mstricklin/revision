// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.shadowMap;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class TestShadowMapSize {
    private ShadowMap<String, String> cm;
    @Before
    public void before() {
        cm = new ShadowMap();
    }
    // =================================
    Map<String, String> someMap0 = ImmutableMap.of("K1", "V1", "K2", "V2", "K3", "V3");
    Map<String, String> someMap1 = ImmutableMap.of("K1", "Va1", "K2", "Va2", "K3", "Va3");
    Map<String, String> someMap2 = ImmutableMap.of("Ka1", "V1", "Ka2", "Va2", "Ka3", "Va3");


    // =================================
    @Test
    public void sizePut() {
        assertEquals(0, cm.size());
        cm.put("K1", "V1a");
        assertEquals(1, cm.size());
        cm.put("K1", "V1b");
        assertEquals(1, cm.size());
        cm.put("K2", "V2");
        assertEquals(2, cm.size());
        cm.rollback();
        assertEquals(0, cm.size());

        IntStream.range(0, 10)
                 .mapToObj(Integer::toString)
                 .forEach(i -> {
                     cm.put("K" + i, "V" + i);
                 });
        cm.commit();
        assertEquals(10, cm.size());
        IntStream.range(10, 20)
                 .mapToObj(Integer::toString)
                 .forEach(i -> {
                     cm.put("K" + i, "V" + i);
                 });

        assertEquals(20, cm.size());

    }
    @Test
    public void sizePutDups() {
        assertEquals(0, cm.size());
        cm.put("K1", "V1");
        assertEquals(1, cm.size());
        cm.put("K1", "V1");
        assertEquals(1, cm.size());

        cm.rollback();
        assertEquals(0, cm.size());

        cm.put("K1", "V1");
        cm.commit();
        cm.put("K1", "V1");
        cm.commit();
        assertEquals(1, cm.size());

        cm.clear();
        assertEquals(0, cm.size());
        cm.commit();
        assertEquals(0, cm.size());

        cm.dumpAll();


        IntStream.range(0, 10)
                 .mapToObj(Integer::toString)
                 .forEach(i -> {
                     cm.put("K" + i, "V" + i);
                     cm.put("K" + i, "Va" + i);
                 });
        cm.dumpAll();
        cm.commit();
        assertEquals(10, cm.size());
    }
    @Test
    public void sizeRm() {
        assertEquals(0, cm.size());
        cm.put("K1", "V1");
        assertEquals(1, cm.size());
        cm.remove("K1");
        assertEquals(0, cm.size());

        cm.put("K1", "V1");
        assertEquals(1, cm.size());
        cm.commit();
        cm.remove("K1");
        assertEquals(0, cm.size());

        cm.put("K2", "V2");
        assertEquals(1, cm.size());
    }
    @Test
    public void sizeRmMissing() {
        assertEquals(0, cm.size());
        cm.put("K1", "V1");
        assertEquals(1, cm.size());
        cm.remove("K1");
        assertEquals(0, cm.size());
        cm.remove("K1");
        assertEquals(0, cm.size());
        cm.remove("K99");
        assertEquals(0, cm.size());
    }
    @Test
    public void sizePutAll() {
        cm.putAll(someMap0);
        assertEquals(3, cm.size());
        cm.rollback();
        assertEquals(0, cm.size());
        cm.putAll(someMap0);
        assertEquals(3, cm.size());
        cm.commit();
        assertEquals(3, cm.size());
        cm.putAll(someMap0);
        assertEquals(3, cm.size());
        cm.putAll(someMap1);
        assertEquals(3, cm.size());

        cm.putAll(someMap2);
        assertEquals(6, cm.size());
        cm.rollback();
        assertEquals(3, cm.size());
    }
    @Test
    public void sizeClear() {
        cm.putAll(someMap0);
        assertEquals(3, cm.size());
        cm.commit();
        assertEquals(3, cm.size());
        cm.clear();
        assertEquals(0, cm.size());
        cm.rollback();
        assertEquals(3, cm.size());
    }
    @Test
    public void size() {
        // changes size:
        // 1. X put
        // 2. X remove
        // 3. X putAll
        // 4. X clear
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

        cm.clear();
        assertEquals(0, cm.size());
        cm.rollback();
        assertEquals(1, cm.size());

        cm.clear();
        cm.commit();
        assertEquals(0, cm.size());

        cm.put("K1", "V1");
        cm.put("K1", "V1");
        assertEquals(1, cm.size());
        cm.clear();
        cm.commit();

        cm.put("K1", "V1");
        cm.commit();
        cm.put("K1", "V1");
        cm.dumpAll();
        assertEquals(1, cm.size());
        cm.clear();
        cm.commit();


//        Map<String, String> m = ImmutableMap.of("K1", "V1", "K2", "V2", "K3", "V3");
//        cm.putAll(m);
//        assertEquals(3, cm.size());

        // putAll
        // clear
    }
    // =================================
}
