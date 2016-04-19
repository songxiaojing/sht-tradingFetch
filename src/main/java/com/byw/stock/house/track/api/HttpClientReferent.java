package com.byw.stock.house.track.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class HttpClientReferent {

    /**
     * time out of try to connect.
     */
    public int _connect_timeout_millis = 100000;
    /**
     * client work thread pool size.
     */
    public int _nioEventLoopGroupSize = 5;

    /**
     * header of request.
     */
    public HashMap<String, String> _requestHeaderMap = new HashMap<String, String>();
    /**
     * cookies of request.
     */
    public Set<Cookie> _requestCookies = new HashSet<Cookie>();
    /**
     * request host.
     */
    public String _host = null;
    /**
     * request path.
     */
    public String _path = null;
    /**
     * request port.
     */
    public int _port = 80;
    /**
     * body of request.
     */
    public String _requestContent = "";
    /**
     * is HTTP or HTTPS
     */
    public boolean _enableSSL = false;
    // RESPONSE 
    /**
     * decode for response body.
     */
    public Charset _responseBodyCharset = Charset.forName("GBK");
    /**
     * response status.
     */
    public HttpResponseStatus _responseStatus = null;
    /**
     * listener for response.
     */
    public IResponseListener _responseListener;
    /**
     * Your field description in here.
     */
    public HashMap<String, String> _responseHeaderMap = new HashMap<String, String>();

    public static HttpClientReferent buildHttpClientReferent(String host, String path, IResponseListener responseListener) {

        HttpClientReferent hcr = new HttpClientReferent();
        hcr._host = host;
        hcr._path = path;
        hcr._responseListener = responseListener;
        return hcr;
    }

    private HttpClientReferent() {

    }

    /**
     * add header value .
     * 
     * @param key
     * @param value
     */
    public void addRequestHeader(String key, String value) {

        _requestHeaderMap.put(key, value);
    }

    /**
     * set cookies.
     * 
     * @param key
     * @param value
     */
    public void addRequestCookies(String key, String value) {

        _requestCookies.add(new DefaultCookie(key, value));
    }

    /**
     * set response header.
     * 
     * @param key
     * @param value
     */
    public void addResponseHeader(String key, String value) {

        _responseHeaderMap.put(key, value);
    }
}
