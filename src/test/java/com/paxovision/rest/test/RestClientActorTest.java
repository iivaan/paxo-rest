package com.paxovision.rest.test;

import com.google.common.collect.FluentIterable;
import com.paxovision.rest.actor.RestClientActor;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.paxovision.rest.test.WireMockSetupExtension.WIREMOCK_SERVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


@ExtendWith(WireMockSetupExtension.class)
public class RestClientActorTest {
    private static final String XML_CONTENT_TYPE = "application/xml; charset=UTF-8";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    private final Supplier<String> defaultDynamicHeaderSupplier =
            () -> String.valueOf(LocalDate.now().getMonthValue());
    private final Supplier<String> dynamicHeaderSupplier =
            () -> String.valueOf(Year.now().getValue());

    private RestClientActor restClientActor;

    @BeforeEach
    public void reset() {
        // reset all mock REST server configuration before each test
        WIREMOCK_SERVER.resetAll();
        // create new TestRail client instance
        restClientActor =
        RestClientActor.newBuilder()
                .withBaseURL("http://localhost:" + WIREMOCK_SERVER.port())
                .withBasicAuth("User", "Password")
                .withDefaultHeader("DefaultStaticHeader", "static-header-value")
                .withDefaultHeader("DefaultDynamicHeader", defaultDynamicHeaderSupplier)
                .build();
    }

    @Test
    public void executeWithoutExpect() {
        //System.out.println(FluentIterable.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm());
        stubFor(
                get(urlMatching(".*/api/v2/.*"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                                        .withBody("{}")));

        restClientActor
                .get("/api/v2/get_bookmark/123")
                .withHeader("Content-Type", JSON_CONTENT_TYPE)
                .withHeader("DynamicHeader", dynamicHeaderSupplier)
                .withHeaders(ImmutableMap.of("headerl", "valuel", "header2", "value2"))
                .execute();
        verify(
                getRequestedFor(urlMatching(".*/api/v2/get_bookmark/123"))
                        .withHeader("Content-Type", equalTo(JSON_CONTENT_TYPE))
                        .withHeader("DefaultStaticHeader", equalTo("static-header-value"))
                        .withHeader("DefaultDynamicHeader", equalTo(defaultDynamicHeaderSupplier.get()))
                        .withHeader("DynamicHeader", equalTo(dynamicHeaderSupplier.get()))
                        .withHeader("headerl", equalTo("valuel"))
                        .withHeader("header2", equalTo("value2"))
                        .withHeader(AUTHORIZATION, matching("Basic.*==")));

    }

    @Test
    public void sslTestNoLogging() {
        stubFor(
                get(urlMatching(".*/api/v2/.*"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                                        .withBody("{}")));
        restClientActor =
                RestClientActor.newBuilder()
                        .withBaseURL("https://localhost:" + WireMockSetupExtension.HTTPS_PORT)
                        .skipSSLChecks()
                        .disableLogging()
                        .build();

        restClientActor
                .get("/api/v2/test")
                .expect(response -> response.match().accepted().bodyAsString("{}"));

    }

    @Test
    public void overrideDefaultHeaderTest() {
        stubFor(
                get(urlMatching(".*/api/v2/.*"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                                        .withBody("{}")));

        restClientActor =
                RestClientActor.newBuilder()
                        .withBaseURL("http://localhost:" + WIREMOCK_SERVER.port())
                        .withDefaultHeader("Content-Type", JSON_CONTENT_TYPE)
                        .build();

        restClientActor
            .get("/api/v2/test")
                .withHeader("Content-Type", "OVERRIDEN")
                .expect(response -> response.match().accepted());

        verify(
                getRequestedFor(urlMatching(".*/api/v2/test"))
                        .withHeader("Content-Type", equalTo("OVERRIDEN")));

    }


    @Test
    public void overrideDefaultHeadersTest() {
        stubFor(
                get(urlMatching(".*/api/v2/.*"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                                        .withBody("{}")));

        restClientActor =
                RestClientActor.newBuilder()
                        .withBaseURL("http://localhost:" + WIREMOCK_SERVER.port())
                        .withDefaultHeaders(
                                ImmutableMap.of(
                                        "Content-Type", JSON_CONTENT_TYPE, "headerl", "valuel"))
                        .build();

        restClientActor
                .get("/api/v2/test")
                .withHeader("headerl", "value2")
                .expect(response -> response.match().accepted());

        verify(
                getRequestedFor(urlMatching(".*/api/v2/test"))
                        .withHeader("Content-Type", equalTo(JSON_CONTENT_TYPE))
                        .withHeader("headerl", equalTo("value2")));

    }




}
