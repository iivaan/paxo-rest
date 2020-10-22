package com.paxovision.rest.request;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
/** REST PATCH request builder */
public class RestPatchRequestBuilder extends RestGenericRequestBuilder<RestPatchRequestBuilder> {

    public RestPatchRequestBuilder(String url, OkHttpClient okHttpClient) {
        super(url, okHttpClient);
        // add default body since PATCH w/o body sent as GET
        requestBuilder.patch(Util.EMPTY_REQUEST);
    }

    /**
     *	Set request body contentType and actual content (for String payload)
     *
     *	@return self
     */
    public RestPatchRequestBuilder withBody( @Nonnull String contentType, @Nonnull String bodyContent) {
        requestBuilder.patch(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for String payload)
     *
     * @return self
     */
    public RestPatchRequestBuilder withBody(@Nonnull String bodyContent) {
        requestBuilder.patch(createRequestBody(headerContentType, bodyContent));
        return this;
    }

    /**
     *	Set request body contentType and actual content (for byte[] payload)
     *
     *	@return self
     */
    public RestPatchRequestBuilder withBody(@Nonnull String contentType, byte[] bodyContent) {
        requestBuilder.patch(createRequestBody(checkNotNull(contentType), bodyContent));
        return this;
    }

    /**
     *	Set request body actual content (for byte[] payload)
     *
     * @return self
     */
    public RestPatchRequestBuilder withBody(byte[] bodyContent) {
        requestBuilder.patch(createRequestBody(headerContentType, bodyContent));
        return this;
    }

}
