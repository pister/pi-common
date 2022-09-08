package com.github.pister.common.lang.codec;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Created by songlihuang on 2020/6/24.
 */
public class Base629Test extends TestCase {

    private Random random = new Random(System.currentTimeMillis());

    public void testEncode() throws Exception {
        String rs1 = "aksasxzc,asd\u0001\u0002as中国asdxaf62alksjfsaklfjaslfjl哇塞多撒考虑对方静安寺来看待结案SD卡史黛拉数据的卡拉时间答复卡时间分开了啊";
        //String rs1 = "abcd";
        byte[] r1 = rs1.getBytes("utf-8");
        byte[] e1 = Base629.encode(r1);
        System.out.println("length r1:" + r1.length);
        System.out.println("length e1:" + e1.length);
        System.out.println("e1:" + new String(e1, "utf-8"));
        byte[] r2 = Base629.decode(e1);
        String rs2 = new String(r2, "utf-8");
        System.out.println("rs2:" + rs2);
        Assert.assertEquals(rs1, rs2);
    }

    private void assertEncodeDecodes(byte[] data) {
        byte[] e1 = Base629.encode(data);
        byte[] deData = Base629.decode(e1);
        Assert.assertEquals(data.length, deData.length);
        for (int i = 0; i < data.length; i++) {
            Assert.assertEquals(data[i], deData[i]);
        }
    }

    private float getLengthRate(byte[] data) {
        int len1 = data.length;
        byte[] e1 = Base629.encode(data);
        int len2 = e1.length;
        return ((float) len2) / len1;
    }

    private byte[] genBytes(int count) {
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = (byte) random.nextInt(256);
        }
        return bytes;
    }

    public void testDataSet() {
        for (int i = 0; i < 100; i++) {
            byte[] b = genBytes(random.nextInt(200000));
            assertEncodeDecodes(b);
        }
    }

    public void testLengthRate() {
        for (int i = 0; i < 100; i++) {
            int len = random.nextInt(20000);
            byte[] b = genBytes(len);
            System.out.println(getLengthRate(b) + " by length:" + len);
        }
    }

    public void no_testBenchmark() {
        benchmarkEncode(36, 10000);
        benchmarkDecode(36, 10000);
        benchmarkEncode(36, 10 * 10000);
        benchmarkDecode(36, 10 * 10000);
        benchmarkEncode(36, 100 * 10000);
        benchmarkDecode(36, 100 * 10000);
        benchmarkEncode(100, 10000);
        benchmarkDecode(100, 10000);
        benchmarkEncode(100, 10 * 10000);
        benchmarkDecode(100, 10 * 10000);
        benchmarkEncode(100, 100 * 10000);
        benchmarkDecode(100, 100 * 10000);
        benchmarkEncode(1000, 10000);
        benchmarkDecode(1000, 10000);
        benchmarkEncode(1000, 10 * 10000);
        benchmarkDecode(1000, 10 * 10000);
        benchmarkEncode(1000, 100 * 10000);
        benchmarkDecode(1000, 100 * 10000);
    }

    private void benchmarkEncode(int bytesLength, int loop) {
        byte[] b1 = genBytes(bytesLength);
        long ts1 = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            Base629.encode(b1);
        }
        long delta = System.currentTimeMillis() - ts1;
        System.out.println("[encode] byte size:" + bytesLength + ", loop:" + loop + ", escape:" + delta + " ms");
    }


    private void benchmarkDecode(int bytesLength, int loop) {
        byte[] b1 = genBytes(bytesLength);
        long ts1 = System.currentTimeMillis();
        byte[] e = Base629.encode(b1);
        for (int i = 0; i < loop; i++) {
            Base629.decode(e);
        }
        long delta = System.currentTimeMillis() - ts1;
        System.out.println("<decode> byte size:" + bytesLength + ", loop:" + loop + ", escape:" + delta + " ms");
    }
}