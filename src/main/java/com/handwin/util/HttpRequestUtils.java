package com.handwin.util;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class HttpRequestUtils {
    private static int connectTimeOut = 5000;
    private static int readTimeOut = 10000;
    private static final Logger logger = Logger.getLogger(HttpRequestUtils.class);
    private static String requestEncoding = "UTF-8";

    public static int getConnectTimeOut() {
        return connectTimeOut;
    }

    public static void setConnectTimeOut(int connectTimeOut) {
        HttpRequestUtils.connectTimeOut = connectTimeOut;
    }

    public static int getReadTimeOut() {
        return readTimeOut;
    }

    public static void setReadTimeOut(int readTimeOut) {
         HttpRequestUtils.readTimeOut = readTimeOut;
    }

    public static String getRequestEncoding() {
        return requestEncoding;
    }

    public static void setRequestEncoding(String requestEncoding) {
        HttpRequestUtils.requestEncoding = requestEncoding;
    }

    public static String doGet(String requrl, Map<String, ?> parameters, Header[] headers) {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(requrl);
        if(headers != null) {
            for(Header header : headers)
                getMethod.setRequestHeader(header);
        }

        StringBuilder sb = new StringBuilder();
        if(parameters != null && !parameters.isEmpty()) {
            for(String key : parameters.keySet()) {
                sb.append(key).append("=").append(parameters.get(key)).append("&");
            }
            getMethod.setQueryString(sb.toString());
        }
        String res = "";
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if(statusCode == HttpStatus.SC_OK) {
                res = new String(getMethod.getResponseBody());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }

        return res;
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, Header[] headers) {

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(reqUrl);
        if(headers != null) {
            for(Header header : headers)
                post.setRequestHeader(header);
        }
        if(parameters != null && !parameters.isEmpty()) {
            for (String key : parameters.keySet()) {
                post.addParameter(key, parameters.get(key));
            }
        }


        String res = "";
        try {
            int statusCode = httpClient.executeMethod(post);
            if(statusCode == HttpStatus.SC_OK) {
                res = new String(post.getResponseBody());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            post.releaseConnection();
        }

        return res;
    }
}

