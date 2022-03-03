package com.github.pister.common.lang.util;


import com.github.pister.common.lang.codec.Base629;
import com.github.pister.common.lang.codec.Base65;
import com.github.pister.common.lang.codec.Hex;
import com.github.pister.common.lang.codec.Base33;

import java.nio.charset.Charset;

/**
 * Created by songlihuang on 2019/1/9.
 */
public class BytesTextUtil {

    private static final Charset charset = Charset.forName("utf-8");

    /**
     * 二进制数据转成十六进制的字符串
     * @param data
     * @return
     */
    public static String bytesToHex(byte[] data) {
        return new String(Hex.encodeHex(data));
    }

    /**
     * 十六进制的字符串转成二进制数据
     * @param s
     * @return
     */
    public static byte[] hexToBytes(String s) {
        return Hex.decodeHex(s.toCharArray());
    }

    /**
     * base33是一种base32的变种，把字符'1'替换了默认的pad'='，
     * 相比标准的base64/base32更具有适用性，base64/base32在某些场景下会被转义,
     * 比十六进制更加紧凑（十六进制是2倍放大，base33/base32是1.6倍放大，base64是1.33倍放大）
     *
     * @param data
     * @return
     */
    public static String byteToBase33(byte[] data) {
        return new String(Base33.encode(data), charset);
    }

    /**
     * byteToBase33 的逆向操作
     * @param s
     * @return
     */
    public static byte[] base33ToBytes(String s) {
        return Base33.decode(s.getBytes(charset));
    }


    /**
     * base629转换成里包含A-Za-z0-9共62个字符的字符串格式，base629放大比例约是1.4倍
     * @param data
     * @return
     */
    public static String byteToBase629(byte[] data) {
        return new String(Base629.encode(data), charset);
    }

    /**
     * byteToBase629 的逆向操作
     * @param s
     * @return
     */
    public static byte[] base629ToBytes(String s) {
        return Base629.decode(s.getBytes(charset));
    }

    /**
     * base65转换成里包含A-Za-z0-9_-共64个字符的字符串格式，base65放大是1.33倍+1
     * @param data
     * @return
     */
    public static String byteToBase65(byte[] data) {
        return new String(Base65.encode(data), charset);
    }

    /**
     * byteToBase65 的逆向操作
     * @param s
     * @return
     */
    public static byte[] base65ToBytes(String s) {
        return Base65.decode(s.getBytes(charset));
    }


}
