package org.xmlunit.assertj;

import static org.xmlunit.assertj.error.ShouldNotHaveThrown.shouldNotHaveThrown;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPathFactory;
import org.assertj.core.api.AbstractAssert;
import org.xmlunit.builder.Input;

/**
 *	This is almost exact copy of the {@link org.xmlunit.assertj.XmlAssert} class with one issue fixed
 *	to allow integration with AsserJ SoftAssertions (constructor made public instead of private).
 *	Issue was reported upstream: https://github.com/xmlunit/xmlunit/issues/148
 *
 *	<p>If/when the issue is fixed upstream, this class will be removed and replaced with {@link
 *	org.xmlunit.assertj.XmlAssert}
 */
public class XMLAssert extends AbstractAssert<XMLAssert, Object>{

    private DocumentBuilderFactory dbf;
    private XPathFactory xpf;
    private Map<String, String> prefix2Uri;

    public XMLAssert(Object o) {
        super(o, XMLAssert.class);
    }

    public static XMLAssert assertThat(Object o) {
        return new XMLAssert(o);
    }

    public XMLAssert withDocumentBuilderFactory(DocumentBuilderFactory dbf) {
        isNotNull();
        this.dbf = dbf;
        return this;
    }

    public XMLAssert withXPathFactory(XPathFactory xpf) {
        isNotNull();
        this.xpf = xpf;
        return this;
    }

    public XMLAssert withNamespaceContext(Map<String, String> prefix2Uri) {
        isNotNull();
        this.prefix2Uri = prefix2Uri;
        return this;
    }

    public MultipleNodeAssert nodesByXPath(String xPath) {
        isNotNull();
        try {
            return MultipleNodeAssert.create(actual, prefix2Uri, dbf, xpf, xPath);
        } catch (Exception e) {
            throwAssertionError(shouldNotHaveThrown(e));
        }
        return null; // fix compile issue
    }

    public MultipleNodeAssert hasXPath(String xPath) {
        return nodesByXPath(xPath).exist();
    }

    public void doesNotHaveXPath(String xPath) {
        nodesByXPath(xPath).doNotExist();
    }

    public ValueAssert valueByXPath(String xPath) {
        isNotNull();
        try {
            return ValueAssert.create(actual, prefix2Uri, dbf, xpf, xPath);
        } catch (Exception e) {
            throwAssertionError(shouldNotHaveThrown(e));
        }
        return null; // fix compile issue
    }

    public CompareAssert and(Object control) {
        isNotNull();
        try {
            return CompareAssert.create(actual, control, prefix2Uri, dbf);
        } catch (Exception e) {
            throwAssertionError(shouldNotHaveThrown(e));
        }
        return null; // fix compile issue
    }

    public XMLAssert isValid() {
        isNotNull();
        ValidationAssert.create(actual).isValid();
        return this;
    }

    public XMLAssert islnvalid() {
        isNotNull();
        ValidationAssert.create(actual).isInvalid();
        return this;
    }

    public XMLAssert isValidAgainst(Schema schema) {
        isNotNull();
        ValidationAssert.create(actual, schema).isValid();
        return this;
    }

    public XMLAssert isNotValidAgainst(Schema schema) {
        isNotNull();
        ValidationAssert.create(actual, schema).isInvalid();
        return this;
    }

    public XMLAssert isValidAgainst(Object... schemaSources) {
        isNotNull();
        ValidationAssert.create(actual, schemaSources).isValid();
        return this;
    }

    public XMLAssert isNotValidAgainst(Object... schemaSources) {
        isNotNull();
        ValidationAssert.create(actual, schemaSources).isInvalid();
        return this;
    }
}
