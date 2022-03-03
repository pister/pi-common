package com.github.pister.common.security;

import junit.framework.Assert;

/**
 * Created by songlihuang on 2017/11/27.
 */
public class AssertUtil {

    public static void assertEquals(byte[] d1, byte[] d2) {
        if (d1 == d2) {
            return;
        }
        if (d1 == null || d2 == null) {
            Assert.fail("one of input is null");
        }
        Assert.assertEquals(d1.length, d2.length);
        for (int i = 0; i < d1.length; ++i) {
            Assert.assertEquals(d1[i], d2[i]);
        }
    }
}
