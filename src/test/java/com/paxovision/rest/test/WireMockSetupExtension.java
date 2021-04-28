package com.paxovision.rest.test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.UUID;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


/** JUnit5 extension, which performs WireMockServer server initialization/start/stop/shutdown */
public class WireMockSetupExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    //flag to ensure only beforeAll method will be executed only once
    private static boolean started = false;
    // HTTPS port for WireMock
    public static int HTTPS_PORT = 8833;

    // single instance of the REST mock server to be used by all tests
    public static final WireMockServer WIREMOCK_SERVER =
            new WireMockServer(options().dynamicPort().httpsPort(HTTPS_PORT));


    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            started = true;
            // start the mock REST server
            WIREMOCK_SERVER.start();
            // configure WireMock host/port
            WireMock.configureFor("localhost", WIREMOCK_SERVER.port());

            // register a callback hook when the root test context is shut down
            context.getRoot().getStore(GLOBAL).put(UUID.randomUUID(), this);
        }
    }

    @Override
    public void close() {
        // stop and shutdown the mock REST server after all tests are done
        WIREMOCK_SERVER.stop();
        WIREMOCK_SERVER.shutdown();
    }
}
