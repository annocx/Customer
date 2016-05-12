package com.haier.cabinet.customer.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by lzx on 15/9/12.
 */
public class ApiHttpClient {

    public static AsyncHttpClient client;

    public ApiHttpClient() {}

    public static AsyncHttpClient getHttpClient() {
        return client;
    }

    public static void get(String url, AsyncHttpResponseHandler handler) {
        client.get(url, handler);
    }

    public static void get(String url, RequestParams params,
                           AsyncHttpResponseHandler handler) {
        client.get(url, params, handler);
    }

    public static void post(String url, AsyncHttpResponseHandler handler) {
        client.post(url, handler);
    }

    public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler handler) {
        client.post(url, params, handler);
    }

    public static void setHttpClient(AsyncHttpClient httpClient) {
        client = httpClient;
    }
}
