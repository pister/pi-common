package com.github.pister.common.http;

import com.github.pister.common.lang.util.MapUtil;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.DefaultSSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/3/6
 * Time: 下午7:35
 */
public class CommonsHttpClient extends AbstractHttpClient implements com.github.pister.common.http.HttpClient {

    private static final ThreadLocal<org.apache.commons.httpclient.HttpClient> tlHttpClient = new ThreadLocal<org.apache.commons.httpclient.HttpClient>();
    private static Map<com.github.pister.common.http.HttpMethod, HttpMethodFactory> httpMethodFactories = MapUtil.newHashMap();

    static {
        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.POST, new HttpMethodFactory() {
            @Override
            public org.apache.commons.httpclient.HttpMethod createHttpMethod(String url, Map<String, String> headers, InputStream data) {
                PostMethod postMethod = new PostMethod(url);
                prepareForMethod(postMethod, headers);
                RequestEntity entity = new InputStreamRequestEntity(data);
                postMethod.setRequestEntity(entity);
                return postMethod;
            }
        });
        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.GET, (url, headers, data) -> {
            GetMethod getMethod = new GetMethod(url);
            prepareForMethod(getMethod, headers);
            return getMethod;
        });

        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.PUT, (url, headers, data) -> {
            PutMethod putMethod = new PutMethod(url);
            prepareForMethod(putMethod, headers);
            RequestEntity entity = new InputStreamRequestEntity(data);
            putMethod.setRequestEntity(entity);
            return putMethod;
        });

        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.DELETE, (url, headers, data) -> {
            DeleteMethod deleteMethod = new DeleteMethod();
            prepareForMethod(deleteMethod, headers);
            return deleteMethod;
        });

        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.HEADER, (url, headers, data) -> {
            HeadMethod headMethod = new HeadMethod(url);
            prepareForMethod(headMethod, headers);
            return headMethod;
        });

        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.OPTIONS, (url, headers, data) -> {
            OptionsMethod optionsMethod = new OptionsMethod(url);
            prepareForMethod(optionsMethod, headers);
            return optionsMethod;
        });

        httpMethodFactories.put(com.github.pister.common.http.HttpMethod.PATCH, (url, headers, data) -> {
            throw new UnsupportedOperationException();
        });

    }

    private static final Protocol defaultHttpsProtocol = new Protocol("https", (ProtocolSocketFactory) DefaultSSLProtocolSocketFactory.getSocketFactory(), 443);
    private static final Protocol ignoreSSLHttpsProtocol = new Protocol("https", new HttpsProtocolSocketFactory(), 443);

    private static void prepareForMethod(HttpMethodBase httpMethodBase, Map<String, String> headers) {
        Header[] requestHeaders = mapToHeaders(headers);
        for (Header header : requestHeaders) {
            httpMethodBase.setRequestHeader(header);
        }
    }

    private static Map<String, List<String>> headersToMap(Header[] responseHeaders) {
        Map<String, List<String>> ret = MapUtil.newHashMap();
        for (Header responseHeader : responseHeaders) {
            String name = responseHeader.getName();
            String value = responseHeader.getValue();
            List<String> values = ret.get(name);
            if (values == null) {
                values = new ArrayList<String>(4);
                ret.put(name, values);
            }
            values.add(value);
        }
        return ret;
    }

    private static Header[] mapToHeaders(Map<String, String> map) {
        if (MapUtil.isEmpty(map)) {
            return new Header[0];
        }
        Header[] ret = new Header[map.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Header header = new Header();
            header.setName(entry.getKey());
            header.setValue(entry.getValue());
            ret[i] = header;
            i++;
        }
        return ret;
    }

    protected org.apache.commons.httpclient.HttpClient getHttpClient() {
        org.apache.commons.httpclient.HttpClient client = tlHttpClient.get();
        if (client != null) {
            return client;
        }
        client = new org.apache.commons.httpclient.ExtCommonsHttpClient();
        if (useProxy) {
            client.getHostConfiguration().setProxy(hostname, port);
        }
        HttpConnectionManager httpConnectionManager = client.getHttpConnectionManager();
        HttpConnectionManagerParams httpConnectionManagerParams = httpConnectionManager.getParams();
        httpConnectionManagerParams.setConnectionTimeout(timeout);
        httpConnectionManagerParams.setSoTimeout(timeout);
        tlHttpClient.set(client);

        return client;
    }


    public HttpResponse doRequest(com.github.pister.common.http.HttpMethod method, String url, Map<String, String> userHeaders, InputStream data, boolean ignoreSSLCertificate) throws IOException {
        org.apache.commons.httpclient.HttpClient client = getHttpClient();
        org.apache.commons.httpclient.HttpMethod httpMethod = makeHttpMethod(method, url, userHeaders, data);
        HttpMethodResponse httpMethodResponse = executeMethod(client, httpMethod, ignoreSSLCertificate);
        try {
            return new HttpResponse(defaultCharset, httpMethodResponse.status, httpMethodResponse.headers, httpMethodResponse.httpMethod.getResponseBody());
        } finally {
            httpMethodResponse.httpMethod.releaseConnection();
        }
    }

    @Override
    public HttpLargeBodyResponse doRequestForLargeBody(com.github.pister.common.http.HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
        org.apache.commons.httpclient.HttpClient client = getHttpClient();
        org.apache.commons.httpclient.HttpMethod httpMethod = makeHttpMethod(method, url, userHeaders, data);
        HttpMethodResponse httpMethodResponse = executeMethod(client, httpMethod, false);
        return new CommonsHttpLargeBodyResponse(defaultCharset, httpMethodResponse.status, httpMethodResponse.headers, httpMethodResponse.httpMethod);
    }

    public HttpResponse doRequest(com.github.pister.common.http.HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
        return doRequest(method, url, userHeaders, data, false);
    }

    private HttpMethodResponse executeMethod(org.apache.commons.httpclient.HttpClient client, org.apache.commons.httpclient.HttpMethod httpMethod, boolean ignoreSSLCertificate) throws IOException {
        httpMethod.setFollowRedirects(false);
        HostConfiguration hostconfig = new HostConfiguration();
        URI uri = httpMethod.getURI();
        if (useProxy) {
            hostconfig.setProxy(hostname, port);
        }
        if ("http".equalsIgnoreCase(uri.getScheme()) || !uri.isAbsoluteURI()) {
            hostconfig.setHost(uri);
        } else {
            if (ignoreSSLCertificate) {
                hostconfig.setHost(uri.getHost(), uri.getPort(), ignoreSSLHttpsProtocol);
            } else {
                hostconfig.setHost(uri.getHost(), uri.getPort(), defaultHttpsProtocol);
            }
        }
        int status = client.executeMethod(hostconfig, httpMethod, null);
        Header[] responseHeaders = httpMethod.getResponseHeaders();
        return new HttpMethodResponse(status, headersToMap(responseHeaders), httpMethod);

    }

    static class HttpMethodResponse {
        int status;
        Map<String, List<String>> headers;
        org.apache.commons.httpclient.HttpMethod httpMethod;

        public HttpMethodResponse(int status, Map<String, List<String>> headers, org.apache.commons.httpclient.HttpMethod httpMethod) {
            this.status = status;
            this.headers = headers;
            this.httpMethod = httpMethod;
        }
    }

    protected org.apache.commons.httpclient.HttpMethod makeHttpMethod(com.github.pister.common.http.HttpMethod httpMethod, String url, Map<String, String> headers, InputStream data) {
        HttpMethodFactory httpMethodFactory = httpMethodFactories.get(httpMethod);
        if (httpMethodFactory == null) {
            throw new RuntimeException("no method support for:" + httpMethod);
        }
        return httpMethodFactory.createHttpMethod(url, headers, data);
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    interface HttpMethodFactory {
        org.apache.commons.httpclient.HttpMethod createHttpMethod(String url, Map<String, String> headers, InputStream data);
    }

    static class HttpsProtocolSocketFactory implements ProtocolSocketFactory {

        private SSLContext sslcontext;

        private SSLContext createSSLContext() {
            SSLContext sslcontext;
            try {
                sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            }
            return sslcontext;
        }

        private synchronized SSLContext getSSLContext() {
            if (this.sslcontext == null) {
                this.sslcontext = createSSLContext();
            }
            return this.sslcontext;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
            return getSSLContext().getSocketFactory().createSocket(
                    host,
                    port,
                    localAddress,
                    localPort
            );
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, ConnectTimeoutException {
            int timeout = params.getConnectionTimeout();
            if (timeout == 0) {
                return getSSLContext().getSocketFactory().createSocket(host, port, localAddress, localPort);
            } else {
                Socket socket = getSSLContext().getSocketFactory().createSocket();
                SocketAddress localAddr = new InetSocketAddress(localAddress, localPort);
                SocketAddress remoteAddr = new InetSocketAddress(host, port);
                socket.bind(localAddr);
                socket.connect(remoteAddr, timeout);
                return socket;
            }
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return getSSLContext().getSocketFactory().createSocket(host, port);
        }
    }

    private static class CommonsHttpLargeBodyResponse extends HttpLargeBodyResponse {

        private org.apache.commons.httpclient.HttpMethod httpMethod;

        public CommonsHttpLargeBodyResponse(String defaultCharset, int responseCode, Map<String, List<String>> headers, org.apache.commons.httpclient.HttpMethod httpMethod) throws IOException {
            super(defaultCharset, responseCode, headers, httpMethod.getResponseBodyAsStream());
            this.httpMethod = httpMethod;
        }

        public void close() throws IOException {
            super.close();
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
    }
}
