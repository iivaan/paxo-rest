package com.paxovision.rest.test.strapi;

import com.google.common.collect.ImmutableMap;
import com.paxovision.rest.actor.RestClientActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.paxovision.rest.test.WireMockSetupExtension.WIREMOCK_SERVER;

public class RestaurantsTestCases {
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
        restClientActor =
                RestClientActor.newBuilder()
                        .withBaseURL("http://strapi.shiftedtech.com")
                        //.withBasicAuth("User", "Password")
                        .withDefaultHeader("DefaultStaticHeader", "static-header-value")
                        .withDefaultHeader("DefaultDynamicHeader", defaultDynamicHeaderSupplier)
                        .build();
    }

    @Test
    public void executeWithoutExpect() {

        String firstRestaurent = "{\"id\":2,\"name\":\"Swing the Teapot\",\"description\":\"Vegetarian Friendly, Vegan Options, Gluten Free Options\",\"created_at\":\"2019-05-12T21:09:12.000Z\",\"updated_at\":\"2020-07-12T13:27:30.000Z\",\"categories\":[{\"id\":1,\"name\":\"Fast food\",\"created_at\":\"2019-05-12T22:22:40.000Z\",\"updated_at\":\"2019-05-12T22:22:40.000Z\"},{\"id\":3,\"name\":\"Family style\",\"created_at\":\"2019-05-12T22:23:16.000Z\",\"updated_at\":\"2019-05-12T22:23:16.000Z\"}]}";

        restClientActor
                .get("/restaurants")
                .withHeader("Content-Type", JSON_CONTENT_TYPE)
                .withHeader("Acept",JSON_CONTENT_TYPE)
                .expect(
                        response ->
                                response.match()
                                        .accepted()
                                        .statusCode(200)
                                        .bodyAsJSON(json ->
                                                        json.jsonPathAsJSON("[0]")
                                                        .isEqualToJson(firstRestaurent),
                                                    json ->
                                                        json.jsonPathAsInteger("[0].id")
                                                        .isEqualTo(2),
                                                    json ->
                                                            json.jsonPathAsString("[0].name")
                                                            .isEqualToIgnoringCase("swing the teapot")

                                        ));

    }
    @Test
    public void executeWithoutExpect2() {

        restClientActor
                .get("/restaurants")
                .withHeader("Content-Type", JSON_CONTENT_TYPE)
                .withHeader("Acept",JSON_CONTENT_TYPE)
                .expect(
                        response ->
                                response.match()
                                        .accepted()
                                        .statusCode(200)
                                        .bodyAsString(
                                                body ->
                                                        body.contains("Swing the Teapot")

                                        ));

    }
//    @Test
//    public void executeWithoutExpect3() {
//
//        restClientActor
//                .get("/restaurants")
//                .withHeader("Content-Type", JSON_CONTENT_TYPE)
//                .withHeader("Acept",JSON_CONTENT_TYPE)
//                .expect(
//                        response ->
//                                response.match()
//                                        .accepted()
//                                        .statusCode(200)
//                                        .bodyAsString(
//                                                body ->
//                                                        body.json("Swing the Teapot")
//
//                                        ));
//
//    }
}
