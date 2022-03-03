package com.github.pister.common.lang;

import com.github.pister.common.security.DigestUtil;
import com.github.pister.common.lang.util.BytesTextUtil;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by songlihuang on 2019/1/9.
 */
public class BytesTextUtilTest extends TestCase {

    public void test0() {
        String d1 = "uxczijlkamsd12esa";
        String s1 = BytesTextUtil.bytesToHex(d1.getBytes());
        byte[] d2 = BytesTextUtil.hexToBytes(s1);
        Assert.assertEquals(d1, new String(d2));
    }

    public void test3() {
        String d1 = "uxczijlkamsd12esa";
        String s1 = BytesTextUtil.byteToBase33(d1.getBytes());
        byte[] d2 = BytesTextUtil.base33ToBytes(s1);
        Assert.assertEquals(d1, new String(d2));
    }


    public void test4() throws UnsupportedEncodingException {
        byte[] x = DigestUtil.sha256(UUID.randomUUID().toString().getBytes("utf-8"));
        System.out.println(BytesTextUtil.bytesToHex(x));
        System.out.println(BytesTextUtil.byteToBase33(x));
        System.out.println(BytesTextUtil.byteToBase629(x));
    }

}