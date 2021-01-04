package ru.spbstu.telematics.java;

import junit.framework.TestCase;
import org.apache.commons.collections4.BidiMap;

import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    private BidiMap<Integer, String> bm;
    static ArrayList<Integer> ints = new ArrayList<>(Arrays.asList(1, 2, 14, 21, 191));
    static ArrayList<String> strs = new ArrayList<>(Arrays.asList("1", "2", "14", "21", "191"));

    public AppTest( String testName )
    {
        super( testName );
    }

    static <K, V> BidiMap<K, V> fill(BidiMap<K, V> bm, ArrayList<K> keys, ArrayList<V> vals) {
        for (int i = 0; i < keys.size(); i++) {
            bm.put(keys.get(i), vals.get(i));
        }
        return bm;
    }

    static <K, V> boolean equals(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
        return e1.getKey().equals(e2.getKey()) && e1.getValue().equals(e2.getValue());
    }

    static <K, V> boolean equals(BidiMap<K, V> bm1, BidiMap<K, V> bm2) {
        //return bm1.entrySet().equals(bm2.entrySet());
        Set<Map.Entry<K, V>> s1 = bm1.entrySet(), s2 = bm2.entrySet();
        for (Map.Entry<K, V> e1 : s1) {
            boolean done = false;
            for (Map.Entry<K, V> e2 : s2)
                if (equals(e1, e2)) {
                    done = true;
                    break;
                }
            if (!done)
                return false;
        }
        return true;
    }

    public void testGet() {
        assertTrue(
                fill(new BidirectionalHashMap<Integer, String>(), ints, strs).get(1).equals("1") &&
                        fill(new BidirectionalHashMap<Integer, String>(), ints, strs).getKey("14") == 14
        );
    }

    public void testPut() {
        assertTrue(
                equals(fill(new BidirectionalHashMap<Integer, String>(), ints, strs),
                        new BidirectionalHashMap<Integer, String>(new ArrayList<Integer>(Arrays.asList(1, 2, 14, 21, 191)),
                                new ArrayList<String>(Arrays.asList("1", "2", "14", "21", "191"))))
        );
    }

    public void testContains() {
        assertTrue(
                fill(new BidirectionalHashMap<Integer, String>(), ints, strs).containsKey(1) &&
                        fill(new BidirectionalHashMap<Integer, String>(), ints, strs).containsValue("2")
        );
    }

    public void testRemove() {
        BidiMap<Integer, String> bm = fill(new BidirectionalHashMap<Integer, String>(), ints, strs);
        bm.remove(1);
        bm.remove(2, "2");
        bm.removeValue("14");
        assertTrue(
                equals(bm,
                        new BidirectionalHashMap<Integer, String>(new ArrayList<Integer>(Arrays.asList(21, 191)),
                                new ArrayList<String>(Arrays.asList("21", "191"))))
        );

        bm.clear();
        assertTrue(
                equals(bm,
                        new BidirectionalHashMap<Integer, String>())
        );
    }

    public void testInverse() {
        assertTrue(
                equals(fill(new BidirectionalHashMap<String, Integer>(), strs, ints),
                    fill(new BidirectionalHashMap<Integer, String>(), ints, strs).inverseBidiMap())
        );
    }

    public void testSets() {
        assertEquals(fill(new BidirectionalHashMap<String, Integer>(), strs, ints).values(),
                fill(new BidirectionalHashMap<String, Integer>(), strs, ints).inverseBidiMap().keySet());
    }

    public void testApp()
    {
        testGet();
        testPut();
        testContains();
        testRemove();
        testInverse();
        testSets();
    }
}
