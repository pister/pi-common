package com.github.pister.common.lang;

import com.github.pister.common.lang.codec.Base65;
import com.github.pister.common.lang.charsets.DefaultCharsets;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Created by songlihuang on 2021/11/16.
 */
public class Base65Test extends TestCase {

    private Random random = new Random(System.currentTimeMillis());

    private byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        random.nextBytes(bytes);
        return bytes;
    }

    public void testRandoms() {
        for (int i = 0; i < 200; i++) {
            int len = random.nextInt(128) + 1;
            byte[] b = randomBytes(len);
            byte[] s1 = Base65.encode(b);
            System.out.println(new String(s1, DefaultCharsets.UTF_8));
            byte[] b2 = Base65.decode(s1);
            assertBytesEquals(b, b2);
        }
    }

    private void assertBytesEquals(byte[] b1, byte[] b2) {
        if (b1 == b2) {
            return;
        }
        if (b1 == null || b2 == null) {
            Assert.fail();
        }
        Assert.assertEquals(b1.length, b2.length);
        for (int i = 0; i < b1.length; i++) {
            Assert.assertEquals("position:" + i, b1[i], b2[i]);
        }
    }

    public void testDecode() {
        byte[] bytes = Base65.decodeString("zuL");
        for (byte b : bytes) {
            System.out.println(b);
        }
        byte[] b2 = new byte[] {105, 91};
        System.out.println(Base65.encodeString(b2));
    }

    public void testEncode() throws Exception {
        {
            byte[] d1 = new byte[]{0x1, 0x2, 0x3};
            byte[] s1 = Base65.encode(d1);
            System.out.println(new String(s1, DefaultCharsets.UTF_8));
            for (byte b : Base65.decode(s1)) {
                System.out.println(b);
            }
        }
        {
            byte[] d1 = new byte[]{0x1, 0x2, 0x3, 0x4};
            byte[] s1 = Base65.encode(d1);
            System.out.println(new String(s1, DefaultCharsets.UTF_8));
            for (byte b : Base65.decode(s1)) {
                System.out.println(b);
            }
        }
        {
            byte[] d1 = new byte[]{0x1, 0x2, 0x3, 0x4, 0x5};
            byte[] s1 = Base65.encode(d1);
            System.out.println(new String(s1, DefaultCharsets.UTF_8));
            for (byte b : Base65.decode(s1)) {
                System.out.println(b);
            }
        }
        {
            byte[] d1 = new byte[]{0x1, 0x2, 0x3, 0x4, 0x5, 0x6};
            byte[] s1 = Base65.encode(d1);
            System.out.println(new String(s1, DefaultCharsets.UTF_8));
            for (byte b : Base65.decode(s1)) {
                System.out.println(b);
            }
        }
    }

}