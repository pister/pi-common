package org.apache.commons.httpclient;



import java.io.IOException;

/**
 * Created by songlihuang on 2018/12/28.
 */
public class ExtCommonsHttpClient extends HttpClient {
    public int executeMethod(HostConfiguration hostconfig,
                             final HttpMethod method, final HttpState state)
            throws IOException, HttpException {

       // LOG.trace("enter HttpClient.executeMethod(HostConfiguration,HttpMethod,HttpState)");

        if (method == null) {
            throw new IllegalArgumentException("HttpMethod parameter may not be null");
        }
        HostConfiguration defaulthostconfig = getHostConfiguration();
        if (hostconfig == null) {
            hostconfig = defaulthostconfig;
        }

        HttpMethodDirector methodDirector = new HttpMethodDirector(
                getHttpConnectionManager(),
                hostconfig,
                this.getParams(),
                (state == null ? getState() : state));
        methodDirector.executeMethod(method);
        return method.getStatusCode();
    }
}
