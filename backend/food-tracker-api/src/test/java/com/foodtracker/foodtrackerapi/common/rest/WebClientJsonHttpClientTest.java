package com.foodtracker.foodtrackerapi.common.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * <p>
 * Tests for {@link WebClientJsonHttpClient}. The test starts a small HTTP
 * server of the Java library, so that the test needs no network connection and
 * no external API.
 * </p>
 *
 * @author Janina Mattes
 */
class WebClientJsonHttpClientTest {

    private HttpServer server;
    private WebClientJsonHttpClient httpClient;
    private final AtomicReference<String> lastQuery = new AtomicReference<>();

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private void respondWith(String path, int status, String body) {
        server.createContext(path, exchange -> {
            lastQuery.set(exchange.getRequestURI().getQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        httpClient = new WebClientJsonHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("getJson gives the body of the answer")
    void getJsonGivesBody() {
        respondWith("/food", 200, "{\"label\":\"Milk\"}");

        String result = httpClient.getJson(url("/food"), Collections.emptyMap());

        assertThat(result).isEqualTo("{\"label\":\"Milk\"}");
    }

    @Test
    @DisplayName("getJson sends the parameters in the order of the map")
    void getJsonSendsParameters() {
        respondWith("/food", 200, "{}");
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("app_id", "the-app");
        queryParams.put("upc", "123456");

        httpClient.getJson(url("/food"), queryParams);

        assertThat(lastQuery.get()).isEqualTo("app_id=the-app&upc=123456");
    }

    @Test
    @DisplayName("getJson gives an empty text when the server reports an error")
    void getJsonGivesEmptyTextOnServerError() {
        respondWith("/food", 500, "{\"message\":\"server error\"}");

        String result = httpClient.getJson(url("/food"), Collections.emptyMap());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getJson gives an empty text when the server is not available")
    void getJsonGivesEmptyTextWhenServerIsNotAvailable() {
        server.stop(0);

        String result = httpClient.getJson(url("/food"), Collections.emptyMap());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getJson gives an empty text when the address is not valid")
    void getJsonGivesEmptyTextForInvalidUrl() {
        assertThat(httpClient.getJson("not-a-url", Collections.emptyMap())).isEmpty();
    }
}
