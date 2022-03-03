package com.github.pister.common.lang.guid;

import com.github.pister.common.lang.charsets.DefaultCharsets;
import com.github.pister.common.security.DigestUtil;
import com.github.pister.common.lang.util.BytesTextUtil;
import com.github.pister.common.lang.util.BytesUtil;
import com.github.pister.common.lang.util.SystemUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生成全局唯一标识符(16字节)
 * 4bytes machine
 * 4bytes sequence
 * 4bytes time
 * 4bytes random
 * <p>
 * Created by songlihuang on 2021/8/13.
 */
public class GUID {

    private static final byte[] MACHINE_MD5 = DigestUtil.md5(SystemUtil.SYSTEM_INFO.getBytes(DefaultCharsets.UTF_8));

    /**
     * 序列，机器标识的最后4个字节作为初始值
     */
    private static AtomicInteger sequence = new AtomicInteger(BytesUtil.bytes2int(MACHINE_MD5, 12));

    /**
     * 生成一个16bytes 的guid
     *
     * @return
     */
    public static byte[] guid() {
        byte[] data = new byte[16];
        System.arraycopy(MACHINE_MD5, 0, data, 0, 4);
        int seq = sequence.getAndIncrement();
        BytesUtil.int2bytes(seq, data, 4);
        int time = (int) ((System.currentTimeMillis() / 1000) % Integer.MAX_VALUE);
        BytesUtil.long2bytes(time, data, 8);
        int rnd = ThreadLocalRandom.current().nextInt();
        BytesUtil.int2bytes(rnd, data, 12);
        return data;
    }

    /**
     * 生成一个长度为32个字符串的guid字符串（十六进制）
     *
     * @return
     */
    public static String guidHex() {
        return BytesTextUtil.bytesToHex(guid());
    }

    /**
     * 生成一个长度最多不会超过32个字符串的guid字符串（字母和数字组合）
     *
     * @return
     */
    public static String guidAlphanumeric() {
        return guidAlphanumeric(32);
    }

    /**
     * 生成一个guid，长度为 16 * 4 / 3 + 1 = 22 字符，
     * 可能包含的字符有a-z, A-Z , 0-9, _ , -
     * @return
     */
    public static String guidBase65() {
        return BytesTextUtil.byteToBase65(guid());
    }


    /**
     * 生成一个长度最多不会超过maxLength个字符串的guid字符串（字母和数字组合）
     *
     * @param maxLength 最少不能少于27, 实际可能比27更短
     * @return
     */
    public static String guidAlphanumeric(int maxLength) {
        if (maxLength < 27) {
            throw new IllegalArgumentException("maxLength must be greater than 26");
        }
        for (; ; ) {
            String s = BytesTextUtil.byteToBase629(guid());
            if (s.length() <= maxLength) {
                return s;
            }
        }
    }


}
