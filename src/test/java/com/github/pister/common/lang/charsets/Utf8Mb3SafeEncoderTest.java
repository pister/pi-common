package com.github.pister.common.lang.charsets;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Created by songlihuang on 2021/4/25.
 */
public class Utf8Mb3SafeEncoderTest extends TestCase {


   private static  Random random = new Random(System.currentTimeMillis());


    public void testEncodeUtf8mb3Safe() throws Exception {
        //  String s = "asdsa阿斯顿\uD83D\uDE01\uD83D\uDC36\uD83E\uDD14\uD83D\uDC7B\uD83D\uDE92撒多\uD82D\uDE91\uD82C\uDE90 \uD82B\uDE89 \uD81A\uDE69 \uD82A\uDE88";
        for (int i = 0; i < 10; i++) {
            String s = randomSurrogatesString();
            String x = Utf8Mb3SafeEncoder.encode(s);
           // System.out.println(x);
            String s2 = Utf8Mb3SafeEncoder.decode(x);
            Assert.assertEquals(s, s2);
        }
    }

    public void testEscape() {
        String rowData = "你好!!{35s7}阿萨德\uD83D\uDE01\uD83D\uDC36水电费!!!{35s?}!";
        String s2 = Utf8Mb3SafeEncoder.encode(rowData);
        System.out.println(s2);
        String s3 = Utf8Mb3SafeEncoder.decode(s2);
        Assert.assertEquals(s3, rowData);
    }

    private static String randomSurrogatesString() {
        StringBuilder sb = new StringBuilder("xxxx阿斯顿");
        for (int i = 0; i < 100; i++) {
            // uD800-uDBFF
            // uDC00-uDFFF
            sb.append("啊啊");
            char c1 = (char) (random.nextInt(0xDBFF - 0xD800) + 0xD800);
            char c2 = (char) (random.nextInt(0xDFFF - 0xDC00) + 0xDC00);
            sb.append(new char[]{c1, c2});
        }
        return sb.toString();
    }
}