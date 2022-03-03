package com.github.pister.common.security;

import junit.framework.TestCase;

import java.io.*;


/**
 * Created by songlihuang on 2017/11/27.
 */
public class AESUtilTest extends TestCase {

    public void test0() throws UnsupportedEncodingException {
        byte[] key = AESUtil.generateKey256().getEncoded();
        byte[] rawData = "safjklfed,sdflafaf4s#gsdgds".getBytes("utf-8");
        byte[] encryptData = AESUtil.encrypt(key, rawData);
        byte[] d2 = AESUtil.decrypt(key, encryptData);
        AssertUtil.assertEquals(rawData, d2);
    }


}