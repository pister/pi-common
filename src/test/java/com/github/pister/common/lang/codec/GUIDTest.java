package com.github.pister.common.lang.codec;

import com.github.pister.common.lang.guid.GUID;
import com.github.pister.common.lang.util.CollectionUtil;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Set;

/**
 * Created by songlihuang on 2021/8/13.
 */
public class GUIDTest extends TestCase {

    public void test1() {
        int[] count = new int[10];
        for (int i = 0; i < 100000; i++) {
            String s2 = GUID.guidAlphanumeric(32);
            int length = s2.length();
            count[length - 22]++;
        }
        for (int i = 0; i < count.length; i++) {
            System.out.println((i + 22) + ": " + count[i]);
        }
    }

    public void test12() {
        int[] count = new int[10];
        for (int i = 0; i < 100000; i++) {
            String s2 = GUID.guidBase65();
            int length = s2.length();
            count[length - 22]++;
        }
        for (int i = 0; i < count.length; i++) {
            System.out.println((i + 22) + ": " + count[i]);
        }
    }

    public void test2() {
        Set<String> hits = CollectionUtil.newHashSet();
        for (int i = 0; i < 10 * 10000; i++) {
            String s2 = GUID.guidAlphanumeric();
            //String s2 = UUID.randomUUID().toString();
            if (hits.contains(s2)) {
                Assert.fail("exist!!");
            }
            hits.add(s2);
        }
    }
}