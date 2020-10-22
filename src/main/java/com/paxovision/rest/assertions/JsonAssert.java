package com.paxovision.rest.assertions;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import com.paxovision.rest.response.ResponseExtractor;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.assertj.core.api.BigDecimalAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.IntegerAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ProxyableListAssert;
import org.assertj.core.api.ProxyableObjectAssert;
import org.assertj.core.api.StringAssert;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.boot.test.json.JsonContentAssert;


/** JSON assertions bindings for AssertJ */
public class JsonAssert {
    private final DocumentContext actual;
    private final RestClientSoftAssertions softAssertions;
    private final AtomicReference<ResponseExtractor> responseExtractor;
    JsonAssert(RestClientSoftAssertions softAssertions,
               String json,
               AtomicReference<ResponseExtractor> responseExtractor) {

        this.actual = JsonPath.parse(json);
        this.softAssertions = softAssertions;
        this.responseExtractor = responseExtractor;
    }

    /**

     *	Extracts a JSON text using a JsonPath expression and wrap it in a {@link StringAssert}.

     *

     *	@param path JsonPath to extract the string

     *	^return an instance of {@link StringAssert}

     */

    public StringAssert jsonPathAsString(String path) {

        return softAssertions.assertThat(extract(actual.read(path, String.class)));
    }

    /**
    *	Extracts a JSON value prepense using a IsonPath expression and wrap it in a {@link
    *	BooleanAssert}.
    *
    *	@param path JsonPath to check for existence
    *	@return an instance of {@link BooleanAssert}
    */
    public BooleanAssert jsonPathPresent(String path) {

        boolean present = true;
        try {
            actual.read(path);
        } catch (PathNotFoundException e) {
            present = false;
        }
        return softAssertions.assertThat(present).as("JSON path'" + path + "' is present");
    }

    /**
     *	Extracts a ISON number using a JsonPath expression and wrap it in an {@link IntegerAssert}
     *
     *	@param path JsonPath to extract the number
     *	(©return an instance of {(©link IntegerAssert}
     */
    public IntegerAssert jsonPathAsInteger(String path) {
        return softAssertions.assertThat(extract(actual.read(path, Integer.class)));
    }

    /**

     *	Extracts a JSON number using a JsonPath expression and wrap it in an {@link BigDecimalAssert}

     *

     *	@param path JsonPath to extract the number

     *	^return an instance of {@link BigDecimalAssert}

     */

    public BigDecimalAssert jsonPathAsBigDecimal(String path) {

        return softAssertions.assertThat(extract(actual.read(path, BigDecimal.class)));
    }

    /**

     *	Extracts a JSON Boolean using a JsonPath expression and wrap it in an {@link BooleanAssert}

     *

     *	@param path JsonPath to extract the number

     *	^return an instance of {@link BooleanAssert}

     */

    public BooleanAssert jsonPathAsBoolean(String path) {
        return softAssertions.assertThat(extract(actual.read(path, Boolean.class)));
    }

    /**
    *	Extracts a any JSON type using a JsonPath expression and wrap it in an {@link
    *	ProxyableObjectAssert}. Use this method to check for nulls or to do type checks.
    *
    *	@param <T> The generic type of the type field
    *	@param type The type to cast the content of the object, i.e.: {@link String}, {@link Integer}
    *	@param path JsonPath to extract the type
    *	@return an instance of {@link ObjectAssert}
    */
    public <T> ProxyableObjectAssert<Object> jsonPathAs(String path, Class<T> type) {
        return softAssertions.assertThat(extract(actual.read(path, new TypeRef<T>() {})));
    }

    /**

     *	Extracts a any JSON type using a JsonPath expression and wrap it in an {@link

     *	ProxyableObjectAssert}. Use this method to check for nulls or to do type checks.

     *

     *	@param path JsonPath to extract the type

     *	^return an instance of {@link ObjectAssert}

     */

    public ProxyableObjectAssert<Object> jsonPathAsObject(String path) {

        return jsonPathAs(path, Object.class);

    }

    /**
    *	Extracts a ISON array using a IsonPath expression and wrap it in a {@link
    *	ProxyableListAssert}.
    *
    *	@param path JsonPath to extract the array
    *	@param type The type to cast the content of the array, i.e.: {@link String}, {@link Integer}
    *	@param <T> The generic type of the type field
    *	(©return an instance of {(©link ListAssert}
    */
    public <T> ProxyableListAssert<T> jsonPathAsListOf(String path, Class<T> type) {
        return softAssertions.assertThat(extract(actual.read(path, new TypeRef<List<T>>() {})));
    }

    /**
     *	Allows to assert on complete JSON body using {@link
     *	org.springframework.boot.test.json.JsonContentAssert}
     *
     * @return an instance of {@link org.springframework.boot.test.json.JsonContentAssert}
     */
    public JsonContentAssert body() {
        return softAssertions.assertJsonBody(extract(actual.jsonString()));
    }

    /**
    *	Extract JSON path as separate JSON object and allows to assert is separately using {@link
    *	org.springframework.boot.test.json.JsonContentAssert
    *
    *	@param path JsonPath to extract the type
    *	@param assertions pass nested assertions consumers to be executes from selected JSON query
    *	result
    *	^return an instance of {@link org.springframework.boot.test.json.JsonContentAssert}
    */
    public JsonContentAssert jsonPathAsJSON(String path, Consumer<JsonAssert>... assertions) {
        final String json = extract(actual.read(path).toString());
        Stream.of(assertions)
                .forEach(
                        assertion ->
                                assertion.accept(
                                        softAssertions.assertJsonPath(json, responseExtractor)));
        return softAssertions.assertJsonBody(json);
    }

    /**
     *	Extract JSON path as separate JSON object and validate against provided JSON schema
     *
     *	@param jsonSchema for JSON validation
     *	(©return self
     */
    public JsonAssert validateSchema(String jsonSchema) {

        Schema schema = SchemaLoader.load(new JSONObject(jsonSchema));

        try {
            schema.validate(new JSONObject(extract(actual.jsonString())));
        } catch (ValidationException ex) {
            softAssertions.fail("[JSON Schema] " + ex.getMessage());
        }
        return this;
    }

    /** greturn JSON value extractor */
    public JsonAssert extract() {
        responseExtractor.getAndSet(new ResponseExtractor());
        return this;
    }

    private <T> T extract(T value) {
        final ResponseExtractor extractor = responseExtractor.get();
        if (extractor != null) {
            extractor.setValue(value);
        }
        return value;
    }
}
