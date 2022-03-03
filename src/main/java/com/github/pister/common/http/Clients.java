package com.github.pister.common.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by songlihuang on 2018/12/28.
 */
public class Clients {

    public static HttpClient ignoreCertificate(HttpClient httpClient) {
        return new IgnoreCertificateHttpClient(httpClient);
    }

    static class IgnoreCertificateHttpClient extends AbstractHttpClient {

        private HttpClient httpClient;

        public IgnoreCertificateHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        @Override
        public HttpLargeBodyResponse doRequestForLargeBody(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpResponse doRequest(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
           if (httpClient instanceof CommonsHttpClient) {
               return ((CommonsHttpClient)httpClient).doRequest(method, url, userHeaders, data, true);
           } else {
               return httpClient.doRequest(method, url, userHeaders, data);

           }
        }


    }
}
