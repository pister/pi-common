package com.github.pister.common.lang.codec;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Created by songlihuang on 2021/7/28.
 */
public class Long62Test extends TestCase {

    public void testToLong62() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 100* 10000; i++) {
            testValue(random.nextLong());
        }

    }

    private static void testValue(long v) {
        String s = Long62.toString(v);
        long v2 = Long62.parseLong(s);
        Assert.assertEquals(v, v2);
    }


}