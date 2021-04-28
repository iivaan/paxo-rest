package com.paxovision.rest.request;

import com.paxovision.rest.assertions.RestResponseAsserter;
import com.paxovision.rest.exception.PaxoRestException;

import static com.google.common.base.Preconditions.checkNotNull;

import com.paxovision.rest.response.ResponseExtractor;
import com.paxovision.rest.response.RestResponseMatchers;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *	Generic REST request builder, which contains code shared for all the types of the requests (GET
 *	POST, PUT, HEAD, PATCH)
 *
 * @param <S>
 */
public class RestGenericRequestBuilder<S extends RestGenericRequestBuilder<S>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestGenericRequestBuilder.class);
    private static final String MISSING_CONTENT_TYPE_MESSAGE =
            "'Content-Type' value is not specified for the request with non-empty body!";
    private static final String OVERRIDE_CONTENT_TYPE_MESSAGE =
            "'Content-Type' body value '{}' overrides header value '{}'!";

    private final OkHttpClient okHttpClient;
    // value for Content-Type header, specified via the the header builder
    protected String headerContentType;
    // value for Content-Type header, specified via the the body builder
    protected String bodyContentType;

    protected Request.Builder requestBuilder = new Request.Builder();

    RestGenericRequestBuilder(String url, OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        requestBuilder.url(url);
    }

    /**
     *	Add the HTTP header to the request
     *
     *	@param name of the header
     *	@param value of the header
     *	(©return self
     */
    public S withHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            if (bodyContentType != null) {
                LOGGER.warn(OVERRIDE_CONTENT_TYPE_MESSAGE, bodyContentType, value);
            } else {
                headerContentType = value;
            }
        } else {
            requestBuilder.header(name, value);
        }
        return (S) this;
    }

    /**
     *	Add the HTTP header to the request
     *
     *	@param name of the header
     *	@param valueSupplier header value supplier
     *	(©return self
     */
    public S withHeader(String name, Supplier<String> valueSupplier) {
        return withHeader(name, valueSupplier.get());
    }

    /**
     * Add the HTTP headers to the request
     *
     *	@param headers map with request name/value combinations
     *	@return self
     */
    public S withHeaders(Map<String, String> headers) {
        headers.forEach(this::withHeader);
        return (S) this;
    }

    // perform the sync request and capture the response
    private Response executeWithResponse() {
        try {
            return okHttpClient.newCall(requestBuilder.build()).execute();
        } catch (IOException ex) {
            throw new PaxoRestException("Failed to perform REST call: ", ex);
        }
    }

    /** Executes request without applying any of assertions on response */
    public void execute() {
        executeWithResponse();
    }

    /**
     *	Executes request and applies given checkers to the response received
     *
     *	@param <T> type of the return value of the extractor
     *	@param checkers to be applied on response
     *	^return extracted value or null if no extraction requested
     */

    @Nullable
    public <T> T expect(Function<RestResponseMatchers, RestResponseAsserter> checkers) {
        final AtomicReference<ResponseExtractor> responseExtractor = new AtomicReference<>();
        final Response response = executeWithResponse();

        // apply all the assetsions on the response
        checkers.apply(new RestResponseMatchers(response, responseExtractor)).assertAll();

        // return extracted value (if any) or complete response
        final ResponseExtractor extractor = responseExtractor.get();

        if (extractor != null) {
            return (T) extractor.getValue();
        }

        // return nothing if extractor was not used
        return null;
    }

    /**
    *	Creates RequestBody for given contentType and bodyContent
    *
    *	@param contentType of the body	j
    *	@param bodyContent String content of the body
    *	@return created RequestBody instance
    */
    protected RequestBody createRequestBody(@Nullable String contentType, @Nonnull String bodyContent) {
        return createRequestBody(contentType, mediaType -> RequestBody.create(mediaType, checkNotNull(bodyContent)));
    }

    /**
     * 	Creates RequestBody for given contentType and bodyContent
     *
     *	@param contentType of the body
     *	@param bodyContent byte array content of the body
     *	@return created RequestBody instance
     */
    protected RequestBody createRequestBody(@Nullable String contentType, @Nonnull byte[] bodyContent) {
        return createRequestBody(
                contentType, mediaType -> RequestBody.create(mediaType, checkNotNull(bodyContent)));
    }

    // helper method for creation request body
    private RequestBody createRequestBody(String contentType, Function<MediaType, RequestBody> requestBodyCreator) {

        bodyContentType = contentType;
        if (contentType == null) {
            LOGGER.warn(MISSING_CONTENT_TYPE_MESSAGE);
            return requestBodyCreator.apply(null);
        } else {
            if (!contentType.equals(headerContentType)) {
                LOGGER.warn(OVERRIDE_CONTENT_TYPE_MESSAGE, contentType, headerContentType);
            }
            return requestBodyCreator.apply(MediaType.parse(contentType));
        }
    }









}
