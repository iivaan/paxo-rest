package com.paxovision.rest.request;

import okhttp3.OkHttpClient;

/** REST GET request builder */
public class RestGetRequestBuilder extends RestGenericRequestBuilder<RestGetRequestBuilder> {

    public RestGetRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        requestBuilder.get();
    }

    @Override
    public RestGetRequestBuilder withHeader(String name, String value) {
        // do not delay Content-Type header configuration for GET request
        requestBuilder.header(name, value);
        return this;
    }
}

