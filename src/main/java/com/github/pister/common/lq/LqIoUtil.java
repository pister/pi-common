package com.github.pister.common.lq;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午10:57
 */
public class LqIoUtil {

    public static int getDataSum(byte[] data, int start, int len) {
        int sum = 0;
        for (int i = start; i < len; ++i) {
            sum = 13 * sum + data[i];
        }
        return Math.abs(sum) % 128;
    }

    public static int getDataSum(byte[] data) {
        return getDataSum(data, 0, data.length);
    }



}
