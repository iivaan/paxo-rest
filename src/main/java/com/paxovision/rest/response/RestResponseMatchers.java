package com.paxovision.rest.response;



import java.util.concurrent.atomic.AtomicReference;

import com.paxovision.rest.assertions.Matchers;
import com.paxovision.rest.assertions.RestResponseAsserter;
import okhttp3.Response;
/** Implementation of {@link com.paxovision.rest.assertions.Matchers} for the response verifications */
public class RestResponseMatchers extends Matchers<Response> {

    private final AtomicReference<ResponseExtractor> responseExtractor;

    public RestResponseMatchers(Response response, AtomicReference<ResponseExtractor> responseExtractor) {
        super(response);
        this.responseExtractor = responseExtractor;
    }

    @Override
    public RestResponseAsserter match() {
        return new RestResponseAsserter(getMatchingObject( ), responseExtractor);
    }

}

