package com.paxovision.rest.request;

import okhttp3.OkHttpClient;

/** REST GET request builder */
public class RestHeadRequestBuilder extends RestGenericRequestBuilder<RestHeadRequestBuilder>{

    public RestHeadRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        requestBuilder.head();
    }

    @Override
    public RestHeadRequestBuilder withHeader(String name, String value) {
        // do not delay Content-Type header configuration for HEAD request
        requestBuilder.header(name, value);
        return this;
    }

}