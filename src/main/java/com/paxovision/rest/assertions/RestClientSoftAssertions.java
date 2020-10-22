package com.paxovision.rest.assertions;

import com.paxovision.rest.response.ResponseExtractor;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.SoftAssertions;
import org.springframework.boot.test.json.JsonContentAssert;
import org.xmlunit.assertj.XMLAssert;

/** AssertJ Soft Assertions for {@link com.paxovision.rest.actor.RestClientActor} */
public class RestClientSoftAssertions extends SoftAssertions {

    /**
     *	Returns {@link HeaderAssert} assertions for the Map value
     *
     *	@param headers headers names and values as map
     *	@param responseExtractor response extractor
     *	@return {@link HeaderAssert}
     */
    public HeaderAssert assertHeaders(
            Map<String, String> headers, AtomicReference<ResponseExtractor> responseExtractor) {
        return new HeaderAssert(this, headers, responseExtractor);
    }

    /**
     *	Returns {@link com.paxovision.rest.assertions.HtmlAssert} assertions for the String value
     *
     *	@param htmlBody HTML value as String
     *	@param responseExtractor response extractor
     *	@return {@link org.xmlunit.assertj.XMLAssert} assertions proxy
     */
    public HtmlAssert assertHtml(String htmlBody, AtomicReference<ResponseExtractor> responseExtractor) {
        return new HtmlAssert(this, htmlBody, responseExtractor);
    }

    /**
    *	Returns {@link org.xmlunit.assertj.XMLAssert} assertions for the String value
    *
    *	@param xmlBody XML value as String
    *	@return {@link org.xmlunit.assertj.XMLAssert} assertions proxy
    */
    public XMLAssert assertXPath(String xmlBody) {
        return proxy(XMLAssert.class, Object.class, xmlBody);
    }

    /**
    *	Returns {@link com.paxovision.rest.assertions.JsonAssert} assertions for the String value
    *
    *	@param jsonBody JSON value as String
    *	@param responseExtractor response extractor
    *	@return {@link com.paxovision.rest.assertions.JsonAssert} assertions
    */
    public JsonAssert assertJsonPath( String jsonBody, AtomicReference<ResponseExtractor> responseExtractor) {
        return new JsonAssert(this, jsonBody, responseExtractor);
    }

    /**
    *	Returns {@link org.springframework.boot.test.json.JsonContentAssert} assertions for the String value
    *
    *	@param jsonBody JSON body as String
    *	@return {@link org.springframework.boot.test.json.JsonContentAssert} assertions proxy
    */
    public JsonContentAssert assertJsonBody(String jsonBody) {
        return proxy(JsonContentAssertExt.class, String.class, jsonBody);
    }

}
