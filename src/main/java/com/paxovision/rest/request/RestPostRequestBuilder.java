package com.paxovision.rest.request;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

/** REST POST request builder */
public class RestPostRequestBuilder extends RestGenericRequestBuilder<RestPostRequestBuilder> {

    public RestPostRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        // add default body since POST w/o body sent as GET
        requestBuilder.post(Util.EMPTY_REQUEST);
    }

    /**
     *	Set request body contentType and actual content (for String payload)
     *	I
     *	@return self
     */
    public RestPostRequestBuilder withBody( @Nonnull String contentType, @Nonnull String bodyContent) {
        requestBuilder.post(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for String payload)
     *
     * @return self
     */
    public RestPostRequestBuilder withBody(@Nonnull String bodyContent) {
        requestBuilder.post(createRequestBody(headerContentType, bodyContent));
        return this;
    }

    /**
     *	Set request body contentType and actual content (for byte[] payload)
     *
     *	@return self
    */
    public RestPostRequestBuilder withBody(@Nonnull String contentType, byte[] bodyContent) {
        requestBuilder.post(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for byte[] payload)
     *
     * @return self
     */
    public RestPostRequestBuilder withBody(byte[] bodyContent) {
        requestBuilder.post(createRequestBody(headerContentType, bodyContent));
        return this;
    }
}
