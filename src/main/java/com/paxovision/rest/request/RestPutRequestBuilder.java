package com.paxovision.rest.request;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

/** REST PUT request builder */
public class RestPutRequestBuilder extends RestGenericRequestBuilder<RestPutRequestBuilder> {

    public RestPutRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        // add default body since PUT w/o body sent as GET
        requestBuilder.put(Util.EMPTY_REQUEST);
    }

    /**
    *	Set request body contentType and actual content (for String payload)
    *	@return self
    */
    public RestPutRequestBuilder withBody(@Nonnull String contentType, @Nonnull String bodyContent) {
        requestBuilder.put(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
    *	Set request body actual content (for String payload)
    *	@return self
    */
    public RestPutRequestBuilder withBody(@Nonnull String bodyContent) {
        requestBuilder.put(createRequestBody(headerContentType, bodyContent));
        return this;
    }

    /**
    *	Set request body contentType and actual content (for byte[] payload)
    *	@return self
    */
    public RestPutRequestBuilder withBody(@Nonnull String contentType, byte[] bodyContent) {
        requestBuilder.put(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for byte[] payload)
     *
     * @return self
     */
    public RestPutRequestBuilder withBody(byte[] bodyContent) {
        requestBuilder.put(createRequestBody(headerContentType, bodyContent));
        return this;
    }
}
