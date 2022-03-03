package com.github.pister.common.lang.codec;


/**
 * 用62进制表示一个long类型的数字
 * Created by songlihuang on 2021/7/28.
 */
public class Long62 {

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private static final int[] CHAR2DIGITS = new int[127];

    static {
        for (int i = '0'; i <= '9'; i++) {
            CHAR2DIGITS[i] = i - '0';
        }
        for (int i = 'a'; i <= 'z'; i++) {
            CHAR2DIGITS[i] = i - 'a' + 10;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            CHAR2DIGITS[i] = i - 'A' + 36;
        }

    }

    private static final int RADIUS = DIGITS.length;

    private Long62() {}

    /**
     * 解析62进制的整数
     * @param input
     * @return
     */
    public static long parseLong(String input) {
        boolean neg = false;
        int pos = 0;
        if (input.charAt(0) == '-') {
            neg = true;
            pos = 1;
        }
        long value = 0;
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            value *= RADIUS;
            value += CHAR2DIGITS[c];
        }
        if (neg) {
            return ~value + 1;
        }
        return value;
    }


    /**
     * 把一个整数转成62进制
     * @param value
     * @return
     */
    public static String toString(long value) {
        if (value == Long.MIN_VALUE) {
            throw new IllegalArgumentException("not support value:" + value);
        }
        boolean neg = false;
        if (value < 0) {
            neg = true;
            value = ~value + 1;
        }
        char[] chars = new char[16];
        int pos = 0;
        for (; ; ) {
            long remain = value / RADIUS;
            int index = (int) (value % RADIUS);
            chars[pos++] = DIGITS[index];
            value = remain;
            if (remain == 0) {
                break;
            }
        }
        StringBuilder stringBuilder = new StringBuilder(16);
        if (neg) {
            stringBuilder.append('-');
        }
        for (int i = pos - 1; i >= 0; i--) {
            stringBuilder.append(chars[i]);
        }
        return stringBuilder.toString();
    }
}
