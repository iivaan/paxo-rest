package com.paxovision.rest.assertions;

import org.springframework.boot.test.json.JsonContentAssert;

/**
 *	Extension for {@link org.springframework.boot.test.json.JsonContentAssert} which allows create
 *	SoftAssertions proxy by providing single parameter String-value constructor
 */
public class JsonContentAssertExt extends JsonContentAssert {
    public JsonContentAssertExt(String json) {
        super(JsonContentAssert.class, json) ;
    }
}


