package com.github.pister.common.lang.charsets;

import java.lang.reflect.Field;


/**
 * 某些情况下非BMP的字符转成utf-8的时候需要多于3字节，从而导致某些异常，比如 mysql-5.5.3之前的版本；
 * 该类用于那些字符串中含有非BMP字符的转义，
 * 关于unicode, BMP和surrogate可以参考，
 * <a href="https://www.unicode.org/glossary/#surrogate_character"><i>surrogate_character</i></a>
 * <p>
 * <a href="https://www.unicode.org/reports/tr27/tr27-4.html"><i>Unicode Standard Annex</i></a>
 * <p>
 * Created by songlihuang on 2021/4/25.
 */
public class Utf8Mb3SafeEncoder {

    private static final char[] codingBytes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '-', '+'
    };

    private static final Field valueField;

    static {
        try {
            valueField = String.class.getDeclaredField("value");
            valueField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 把一个java字符串（utf-16）转成3字节utf-8安全的字符串
     * 处理后对于那些非BMP字符由于转义导致整体字符串长度会增加
     *
     * 转义后非BMP的内容将被转成 !!{xxxx} 这种格式，因此非bmp的字符存储会由4个字节增加至8个字节；
     * 另外，为了不影响原字符串中已经存在的内容，原始!!{yyyy}也会被转义，转义的后变成 !!!{yyyy}, 也就是做了一个字节。
     *
     * 编码后请使用 <code>decode</code> 方法界面
     * @param s
     * @return
     */
    public static String encode(String s) {
        if (s == null) {
            return null;
        }
        char[] values;
        try {
            values = (char[])valueField.get(s);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        final int len = values.length;
        CharList charList = new CharList(len);
        for (int i = 0; i < len; i++) {
            char c = values[i];
            if (Character.isHighSurrogate(c)) {
                int code = codePointAtImpl(values, i, len);
                i++;
                encodeCode(charList, code);
            } else {
                if (isEncodedChars(values, i, len)) {
                    escape(charList, values, i);
                    i += 7;
                } else {
                    charList.addChar(c);
                }
            }
        }
        return charList.asString();
    }

    private static boolean isEncodedChars(char[] values, int i, int len) {
        if (i <= len - 8 && values[i] == '!' && values[i + 1] == '!' && values[i + 2] == '{' && values[i + 7] == '}') {
            return true;
        }
        return false;
    }

    private static boolean isEscapeChars(char[] values, int i, int len) {
        if (i <= len - 9 && values[i] == '!' && values[i + 1] == '!' && values[i + 2] == '!' && values[i + 3] == '{' && values[i + 8] == '}') {
            return true;
        }
        return false;
    }

    private static void escape(CharList charList, char[] values, int i) {
        charList.addChar('!');
        charList.addChar('!');
        charList.addChar('!');
        charList.addChar('{');
        charList.addChar(values[i + 3]);
        charList.addChar(values[i + 4]);
        charList.addChar(values[i + 5]);
        charList.addChar(values[i + 6]);
        charList.addChar('}');
    }

    private static void unescape(CharList charList, char[] values, int i) {
        charList.addChar('!');
        charList.addChar('!');
        charList.addChar('{');
        charList.addChar(values[i + 4]);
        charList.addChar(values[i + 5]);
        charList.addChar(values[i + 6]);
        charList.addChar(values[i + 7]);
        charList.addChar('}');
    }

    private static void encodeCode(CharList charList, int code) {
        charList.addChar('!');
        charList.addChar('!');
        charList.addChar('{');
        encode(charList, code);
        charList.addChar('}');
    }

    /**
     * unicode 只定义了 21 个有效为
     * 因此这里用 4个可见字节, 每字节个6位，共计4 * 6 = 24位
     *
     * @param charList
     * @param code
     */
    static void encode(CharList charList, int code) {
        // 000000000 uuuuuuxx xxxxyyyy yyzzzzzz
        // 0011 1111 => 0x3F
        int u = (code >> 18) & 0x3F;
        int x = (code >> 12) & 0x3F;
        int y = (code >> 6) & 0x3F;
        int z = code & 0x3F;
        char uc = codingBytes[u];
        char xc = codingBytes[x];
        char yc = codingBytes[y];
        char zc = codingBytes[z];
        charList.addChar(uc);
        charList.addChar(xc);
        charList.addChar(yc);
        charList.addChar(zc);
    }

    static int decode(char[] sources, int offset) {
        // 000000000 uuuuuuxx xxxxyyyy yyzzzzzz
        // 0011 1111 => 0x3F
        int u = decodeValue(sources[offset]);
        int x = decodeValue(sources[offset + 1]);
        int y = decodeValue(sources[offset + 2]);
        int z = decodeValue(sources[offset + 3]);
        return (u << 18) | (x << 12) | (y << 6) | z;
    }

    static int decodeValue(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('a' <= c && c <= 'z') {
            return c - 'a' + 10;
        }
        if ('A' <= c && c <= 'Z') {
            return c - 'A' + 36;
        }
        if (c == '-') {
            return 62;
        }
        if (c == '+') {
            return 63;
        }
        throw new IllegalArgumentException("unknown char:" + c);
    }



    /**
     * encode 的逆操作
     *
     * @param s
     * @return
     */
    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        char[] values;
        try {
            values = (char[])valueField.get(s);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        final int len = values.length;
        CharList charList = new CharList(len);
        for (int i = 0; i < len; i++) {
            char c = values[i];
            if (isEscapeChars(values, i, len)) {
                unescape(charList, values, i);
                i += 8;
            } else if (isEncodedChars(values, i, len)) {
                int code = decode(values, i + 3);
                char hc = highSurrogate(code);
                char lc = lowSurrogate(code);
                charList.addChar(hc);
                charList.addChar(lc);
                i += 7;
            } else {
                charList.addChar(c);
            }
        }
        return charList.asString();
    }


    private static int codePointAtImpl(char[] a, int index, int limit) {
        char c1 = a[index];
        if (Character.isHighSurrogate(c1) && ++index < limit) {
            char c2 = a[index];
            if (Character.isLowSurrogate(c2)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        return c1;
    }


    private static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10)
                + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
    }

    private static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + Character.MIN_LOW_SURROGATE);
    }


    static class CharList {
        private char[] values;
        private int len;
        private int cap;

        public CharList(int cap) {
            this.len = 0;
            this.cap = cap;
            this.values = new char[cap];
        }

        private void ensureCap() {
            if (len < cap) {
                return;
            }
            int newCap;
            if (cap < 16) {
                newCap = 32;
            } else {
                newCap = (int) (cap * 1.5);
            }
            char[] newValues = new char[newCap];
            System.arraycopy(values, 0, newValues, 0, cap);
            this.cap = newCap;
            this.values = newValues;
        }

        public void addChar(char c) {
            ensureCap();
            values[len++] = c;
        }

        public String asString() {
            return new String(values, 0, len);
        }

        public String toString() {
            return asString();
        }
    }


}
