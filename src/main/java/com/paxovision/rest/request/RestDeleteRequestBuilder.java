package com.paxovision.rest.request;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

/** REST DELETE request builder */
public class RestDeleteRequestBuilder extends RestGenericRequestBuilder<RestDeleteRequestBuilder> {

    public RestDeleteRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        // add default body since DELETE w/o body sent as GET
        requestBuilder.delete(Util.EMPTY_REQUEST);
    }

    /**
    *	Set request body contentType and actual content (for String payload)
    *	@return self
    */
    public RestDeleteRequestBuilder withBody( @Nonnull String contentType, @Nonnull String bodyContent) {
        requestBuilder.delete(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
    *	Set request body contentType and actual content (for String payload)
    *	@return self
    */
    public RestDeleteRequestBuilder withBody(@Nonnull String bodyContent) {
        requestBuilder.delete(createRequestBody(headerContentType, bodyContent));
        return this;
    }

    /**
     *	Set request body contentType and actual content (for byte[] payload)
     *	@return self
    */
    public RestDeleteRequestBuilder withBody(@Nonnull String contentType, byte[] bodyContent) {
        requestBuilder.delete(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for byte[] payload)
     *	@return self
    */
    public RestDeleteRequestBuilder withBody(byte[] bodyContent) {
        requestBuilder.delete(createRequestBody(headerContentType, bodyContent));
        return this;
    }

}
