package com.paxovision.rest.assertions;

import com.paxovision.rest.response.ResponseExtractor;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.ProxyableMapAssert;
import org.assertj.core.api.StringAssert;

public class HeaderAssert {

    private final Map<String, String> actual;
    private final RestClientSoftAssertions softAssertions;
    private final AtomicReference<ResponseExtractor> responseExtractor;

    HeaderAssert(RestClientSoftAssertions softAssertions,
                 Map<String, String> headers,
                 AtomicReference<ResponseExtractor> responseExtractor) {

            this.softAssertions = softAssertions;
            this.actual = headers;
            this.responseExtractor = responseExtractor;
        }

    /**
    *	Assert on all headers as Map
    *
    *	@return instance of {@link org.assertj.core.api.ProxyableMapAssert} to assert headers as map
    */
    public ProxyableMapAssert<String, String> all() {
        return softAssertions.assertThat(extractValue(actual));
    }

    /**
    *	Assert on header with given name as String
    *
    *	@param header name of the header for assertion
    *	^return instance of {@link org.assertj.core.api.StringAssert} to assert header as string
    */
    public StringAssert withName(String header) {
        return softAssertions.assertThat(extractValue(actual.get(header)));
    }

    /** (©return header value extractor */

    public HeaderAssert extract() {
        responseExtractor.getAndSet(new ResponseExtractor());
        return this;
    }

    private <T> T extractValue(T value) {
        final ResponseExtractor extractor = responseExtractor.get();
        if (extractor != null) {
            extractor.setValue(value);
        }
        return value;
    }
}
