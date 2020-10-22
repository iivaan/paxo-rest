package com.paxovision.rest.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.paxovision.rest.exception.PaxoRestException;
import com.paxovision.rest.response.ResponseExtractor;
//import com.mlp.raptor.asserter.Asserter;
import org.xmlunit.assertj.XMLAssert;

import okhttp3.Response;
import okhttp3.ResponseBody;


import java.util.Map;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.ByteArrayAssert;
import org.assertj.core.api.ProxyableMapAssert;
import org.assertj.core.api.ProxyableObjectAssert;
import org.assertj.core.api.StringAssert;


public class RestResponseAsserter implements Asserter<RestClientSoftAssertions> {

    private final Response response;
    private final AtomicReference<ResponseExtractor> responseExtractor;
    private final RestClientSoftAssertions softly = new RestClientSoftAssertions();
    private final ResponseBody responseBody;

    public RestResponseAsserter( Response response, AtomicReference<ResponseExtractor> responseExtractor) {
        this.response = response;
        this.responseBody = response.body();
        this.responseExtractor = responseExtractor;
    }

    /**
    *	Verifies that the status code is in [200..300), which means the request was successfully
    *	received, understood, and accepted.
    *	@return self
    */
    public RestResponseAsserter accepted() {
        assertThat(response.isSuccessful()).isTrue();
        return this;
    }

    /**
    *	Verifies that the status code is equal to the expected one
    *	@param responseCode expected response status code
    *	@return self
    */
    public RestResponseAsserter statusCode(int responseCode) {
        assertThat(response.code()).isEqualTo(responseCode);
        return this;
    }

    /**
    *	Allows to specify custom assertions for the status code
    *	@param responseCode custom assertion consumer
    *	@return self
    */
    public RestResponseAsserter statusCode(Consumer<AbstractIntegerAssert> responseCode) {
        responseCode.accept(assertThat(response.code()));
        return this;
    }

    /**
    *	Perform verification for response header
    *	@param assertions headers assertions to be applied
    *	@return self
    */
    @SafeVarargs
    public final RestResponseAsserter headers(Consumer<HeaderAssert>... assertions) {
        final Map headersMap =
            response.headers().names().stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(), key -> response.headers().get(key)));
        Stream.of(assertions)
                .forEach(
                        assertion ->
                                assertion.accept(
                                        softly.assertHeaders(headersMap, responseExtractor)));
        return this;
    }

    /** @deprecated Please use {@link RestResponseAsserter#headers} instead */
    @SafeVarargs
    @Deprecated
    public final RestResponseAsserter responseHeaders(Consumer<ProxyableMapAssert<String, String>>... assertions) {

        final Map headersMap =
                response.headers().names().stream()
                    .collect(
                            Collectors.toMap(
                                Function.identity(), key -> response.headers().get(key)));
        Stream.of(assertions).forEach(assertion -> assertion.accept(softly.assertThat(headersMap)));
        return this;
    }

    /**
    *	Asserts that response body is empty (null)
    *	@return self
    */
    public RestResponseAsserter noBody() {
        assertThat(responseBody).isNull();
        return this;
    }

    /**
     *	Asserts that response body is equal to the given string value
     *	@param expected response body value
     *	@return self
     */
    public RestResponseAsserter bodyIs(String expected) {
        return bodyAsString(body -> body.isEqualTo(expected));
    }

    /** ^deprecated use {@link RestResponseAsserter#bodyIs} instead */
    @Deprecated
    public RestResponseAsserter bodyAsString(String expected) {
        return bodyIs(expected);
    }

    private RestResponseAsserter assertStringBody(Consumer<String> strBodyAsserter) {
        final String stringBody = bodyAsString();
        softly.assertThat(stringBody).isNotNull() ;
        if (stringBody != null) {
            strBodyAsserter.accept(stringBody);
        }
        return this;
    }

    private RestResponseAsserter assertByteArrayBody(Consumer<byte[]> byteArrayBodyAsserter) {
        final byte[] byteArrayBody = bodyAsByteArray();

        softly.assertThat(byteArrayBody).isNotNull();
        if (byteArrayBody != null) {
            byteArrayBodyAsserter.accept(byteArrayBody);
        }
        return this;
    }

    /**
    * Apply custom assertions for body as String
    *	@param assertions to be applied
    *	@return self
    */
    @SafeVarargs
    public final RestResponseAsserter bodyAsString(Consumer<StringAssert>... assertions) {
        return assertStringBody(
                bodyStr -> {
                    final String strBody = extract(bodyStr);
                            Stream.of(assertions)
                                    .forEach(assertion -> assertion.accept(softly.assertThat(strBody)));
                });
    }

    /**
     * Apply custom assertions for body as XML
     * 	@param assertions to be applied
     *	@return self
     */
    @SafeVarargs
    public final RestResponseAsserter bodyAsXML(Consumer<XMLAssert>... assertions) {
        return assertStringBody(
                bodyStr -> {
                    extract(bodyStr);
                    Stream.of(assertions)
                            .forEach(assertion -> assertion.accept(softly.assertXPath(bodyStr)));
                });
    }

    /**
    * Apply custom assertions for body as DSON
    *	@param assertions to be applied
    *	@return self
    */
    @SafeVarargs
    public final RestResponseAsserter bodyAsJSON(Consumer<JsonAssert>... assertions) {
        return assertStringBody(
                bodyStr -> {
                    extract(bodyStr);
                    Stream.of(assertions)
                            .forEach(
                                    assertion ->
                                            assertion.accept(
                                                    softly.assertJsonPath(
                                                            bodyStr, responseExtractor)));
                });


    }

    /**
    * Apply custom assertions for body as HTML
    *	@param assertions to be applied
    *	@return self
    */
    @SafeVarargs
    public final RestResponseAsserter bodyAsHTML(Consumer<HtmlAssert>... assertions) {

        return assertStringBody(
                bodyStr -> {
                    extract(bodyStr);
                    Stream.of(assertions)
                            .forEach(
                                    assertion ->
                                            assertion.accept(
                                                    softly.assertHtml(bodyStr, responseExtractor)));


                });
    }

    /**
    * Apply custom assertions for body as array of bytes
    *	@param assertions to be applied
    *	@return self
    */
    @SafeVarargs
    public final RestResponseAsserter bodyAsByteArray(Consumer<ByteArrayAssert>... assertions) {
        return assertByteArrayBody(

                bodyByteArray -> {
                    extract(bodyByteArray);
                    Stream.of(assertions)
                            .forEach(
                                    assertion ->
                                            assertion.accept(softly.assertThat(bodyByteArray)));


                });
    }

    /**
     * Converts byte[] body to the requested type using given factory function
     *	@param <T> type of the object to convert byte[] body into
     *	@param instanceFactory factory of the object
     *	@param assertions to be applied on converted object
     *	@return constructed instance of T
     */
    public final <T> RestResponseAsserter bodyAs(Function<byte[], T> instanceFactory, Consumer<ProxyableObjectAssert<T>>... assertions) {
        final T body = extract(instanceFactory.apply(bodyAsByteArray()));
        Stream.of(assertions).forEach(assertion -> assertion.accept(softly.assertThat(body)));
        return this;
    }


    /** @return response body as String */
    private String bodyAsString() {

        try {
            return responseBody.string();
        } catch (IOException ex) {
            throw new PaxoRestException("Failed to retrieve response body as string: ", ex);
        }
    }

    /** @return response body as byte[] */

    private byte[] bodyAsByteArray() {
        try {
            return responseBody.bytes();

        } catch (IOException ex) {
            throw new PaxoRestException("Failed to retrieve response body as byte[]: ", ex);
        }
    }

    /** @return JSON value extractor */
    public RestResponseAsserter extract() {
        responseExtractor.getAndSet(new ResponseExtractor());
        return this;
    }

    private <T> T extract(T value) {
        final ResponseExtractor extractor = responseExtractor.get();
        if (extractor != null && !extractor.isExtracted()) {
            extractor.setValue(value);
        }
        return value;
    }

    @Override
    public RestClientSoftAssertions getAssertions() {
        return softly;
    }


}
