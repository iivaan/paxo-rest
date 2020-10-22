package com.paxovision.rest.assertions;


import com.paxovision.rest.exception.PaxoRestException;
import com.paxovision.rest.response.ResponseExtractor;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.assertj.core.api.DoubleAssert;
import org.assertj.core.api.LongAssert;
import org.assertj.core.api.StringAssert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** HTML assertions implementation */
public class HtmlAssert {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    private final Document actual;
    private final RestClientSoftAssertions softAssertions;
    private final AtomicReference<ResponseExtractor> responseExtractor;

    HtmlAssert(RestClientSoftAssertions softAssertions,
               String html,
               AtomicReference<ResponseExtractor> responseExtractor) {

        this.actual = Jsoup.parse(html);
        this.softAssertions = softAssertions;
        this.responseExtractor = responseExtractor;
    }

    /**
    *	Extracts a HTML text using a CSS selector expression and wrap it in a {@link
    *	RawStringProcessor} to allow transformation before assertion
    *
    *	@param css CSS selector to extract the string from
    *	@return an instance of {@link RawStringProcessor}
    */
    public RawStringProcessor cssSelectorAsRaw(String css) {
        return new RawStringProcessor(extract(getValueUsingSelector(css)));
    }

    /**
     *	Extracts a HTML text using a CSS selector expression and wrap it in a {@link StringAssert}.
     *
     *	@param css CSS selector to extract the string from
     *	^return an instance of {@link StringAssert}
     */
    public StringAssert cssSelectorAsString(String css) {
        return softAssertions.assertThat(extract(getValueUsingSelector(css)));
    }

    /**
     *	Extracts a HTML text using a CSS selector expression and wrap it in a {@link LongAssert}.
     *
     *	@param css CSS selector to extract the integer value from
     *	@return an instance of {@link LongAssert}
     */
    public LongAssert cssSelectorAsLong(String css) {
        return softAssertions.assertThat(
                extract(stringAsNumber(NUMBER_FORMAT, getValueUsingSelector(css)).longValue()));
    }

    /**
     *	Extracts a HTML text using a CSS selector expression and wrap it in a {(©link DoubleAssert}.
     *
     *	(©param css CSS selector to extract the double value from
     *	(©return an instance of {(©link DoubleAssert}
     */
    public DoubleAssert cssSelectorAsDouble(String css) {
        return softAssertions.assertThat(
                extract(stringAsNumber(NUMBER_FORMAT, getValueUsingSelector(css)).doubleValue()) );
    }

    // use the css to get the control and extract text from it
    private String getValueUsingSelector(String css) {
        return actual.selectFirst(css).text();
    }

    // apply CSS selector and convert result to Number if possible
    private static Number stringAsNumber(NumberFormat numberFormat, String value) {
        try {
            return numberFormat.parse(value);
        } catch (ParseException ex) {
            throw new PaxoRestException("Parsing failed: ", ex);
        }
    }

    /** (©return HTML value extractor */
    public HtmlAssert extract() {
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

    // fluent interface section allowing to pre-process the result before applying the assertion
    public class RawStringProcessor {
        private String rawValue;

        private RawStringProcessor(String rawValue) {
            this.rawValue = rawValue;
        }

        /**
         * (©return value as String
         */
        public StringAssert asString() {
            return softAssertions.assertThat(extract(rawValue));
        }

        /**
         * (©return value as Long of throws the exception if parsing fails
         */
        public LongAssert asLong() {
            return softAssertions.assertThat(
                    extract(stringAsNumber(NUMBER_FORMAT, rawValue).longValue()));
        }

        /**
         * (©param numberFormat format for number parsing
         * (©return value as Long with given number format of throws the exception if parsing fails
         */
        public LongAssert asLong(NumberFormat numberFormat) {
            return softAssertions.assertThat(
                    extract(stringAsNumber(numberFormat, rawValue).longValue()));
        }

        /** (©return value as Double of throws the exception if parsing fails */
        public DoubleAssert asDouble() {
            return softAssertions.assertThat(
                    extract(stringAsNumber(NUMBER_FORMAT, rawValue).doubleValue()));
        }

        /**
         *	(©param numberFormat format for number parsing
         *	(©return value as Double with given number format of throws the exception if parsing fails
         */
        public DoubleAssert asDouble(NumberFormat numberFormat) {
            return softAssertions.assertThat(
                    extract(stringAsNumber(numberFormat, rawValue).doubleValue()));
        }

        /**
         *	Define transformation to be applied to the String value
         *
         *	(©param transformation to be applied	J
         *	(©return self
         */
        public RawStringProcessor transform(Function<String, String> transformation) {
            rawValue = transformation.apply(rawValue);
            return this;
        }

    }
}
