package com.github.pister.common.security;

import junit.framework.TestCase;

/**
 * Created by songlihuang on 2017/11/27.
 */
public class DigestUtilTest extends TestCase {

    public void testMd5() throws Exception {
        String data = DigestUtil.md5hex("hello".getBytes("utf-8"));
        System.out.println(data);
        System.out.println(data.length());
    }

    public void testSha1() throws Exception {
        String data = DigestUtil.sha1hex("hello".getBytes("utf-8"));
        System.out.println(data);
        System.out.println(data.length());
    }

    public void testSha256() throws Exception {
        String data = DigestUtil.sha256hex("hello".getBytes("utf-8"));
        System.out.println(data);
        System.out.println(data.length());
    }

    public void testSha512() throws Exception {
        String data = DigestUtil.sha512hex("hello".getBytes("utf-8"));
        System.out.println(data);
        System.out.println(data.length());
    }

}