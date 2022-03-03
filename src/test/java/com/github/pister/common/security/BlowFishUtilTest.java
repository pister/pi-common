package com.github.pister.common.security;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by songlihuang on 2019/1/9.
 */
public class BlowFishUtilTest extends TestCase {

    public void testEncrypt() throws Exception {
        String rawString = "hello world";
        byte[] key = "abcsda".getBytes();
        byte[] encrypted = BlowFishUtil.encrypt(key, rawString.getBytes());
        byte[] rawData = BlowFishUtil.decrypt(key, encrypted);
        Assert.assertEquals(rawString, new String(rawData));
    }

}