package com.github.pister.common.http;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by songlihuang on 2017/5/26.
 */
public class CommonsHttpClientTest extends TestCase {

    public void testHttpGet() throws IOException {
        String url = "https://blog.csdn.net/psh18513234633/article/details/79321343";
        AbstractHttpClient httpClient = new CommonsHttpClient();
        Object response = httpClient.doGetForString(url);
        System.out.println(response);
    }

    public void testHttpGetLargeBody() throws IOException {
        String url = "https://blog.csdn.net/psh18513234633/article/details/79321343";
        AbstractHttpClient httpClient = new CommonsHttpClient();
        HttpLargeBodyResponse response = httpClient.doRequestForLargeBody(HttpMethod.GET, url, null, null);
        System.out.println(response.getResponseString());
        response.close();
    }

    public void testHttpPost() throws IOException {
        String url = "https://www.baidu.com";
        AbstractHttpClient httpClient = new CommonsHttpClient();
        Object response = httpClient.doPost(url, null, new ByteArrayInputStream("{\"success\":true,\"requestOrder\":\"3b167b6afbee41d19686f51ae1cc2136\",\"data\":{\"idCard\":\"4206276667667567155\",\"name\":\"张三\",\"score\":0}}".getBytes("utf-8")));
        System.out.println(response);
    }


}