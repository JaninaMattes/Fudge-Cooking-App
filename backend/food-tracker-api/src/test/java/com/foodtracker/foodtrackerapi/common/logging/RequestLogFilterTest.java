package com.foodtracker.foodtrackerapi.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

/**
 * Tests for {@link RequestLogFilter}.
 *
 * @author Janina Mattes
 */
class RequestLogFilterTest {

    private final RequestLogFilter filter = new RequestLogFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/1");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearTheContextOfTheLogger() {
        MDC.clear();
    }

    private static FilterChain chainThatAnswers(int status) {
        return (servletRequest, servletResponse) ->
                ((MockHttpServletResponse) servletResponse).setStatus(status);
    }

    @Test
    @DisplayName("A request without the header gets a new ID")
    void requestWithoutHeaderGetsANewId() throws Exception {
        filter.doFilter(request, response, new MockFilterChain());

        String id = response.getHeader(RequestLogFilter.REQUEST_ID_HEADER);
        assertThat(id).isNotNull().hasSize(RequestLogFilter.NEW_ID_LENGTH);
    }

    @Test
    @DisplayName("The filter keeps the ID that the client sends")
    void filterKeepsTheIdOfTheClient() throws Exception {
        request.addHeader(RequestLogFilter.REQUEST_ID_HEADER, "client-42");

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestLogFilter.REQUEST_ID_HEADER)).isEqualTo("client-42");
    }

    @Test
    @DisplayName("The filter removes a line break out of the ID of the client")
    void filterRemovesALineBreakOutOfTheId() {
        request.addHeader(RequestLogFilter.REQUEST_ID_HEADER, "a1\nERROR fake line");

        assertThat(RequestLogFilter.requestIdOf(request)).isEqualTo("a1ERRORfakeline");
    }

    @Test
    @DisplayName("An ID with no letter and no number gives a new ID")
    void idWithoutLetterGivesANewId() {
        request.addHeader(RequestLogFilter.REQUEST_ID_HEADER, "!!!");

        assertThat(RequestLogFilter.requestIdOf(request))
                .hasSize(RequestLogFilter.NEW_ID_LENGTH);
    }

    @Test
    @DisplayName("A long ID of a client keeps 36 characters")
    void longIdIsCut() {
        request.addHeader(RequestLogFilter.REQUEST_ID_HEADER, "x".repeat(100));

        assertThat(RequestLogFilter.requestIdOf(request))
                .hasSize(RequestLogFilter.MAX_ID_LENGTH);
    }

    @Test
    @DisplayName("Every log entry of the request holds the ID of the request")
    void idStandsInTheContextOfTheLoggerDuringTheRequest() throws Exception {
        request.addHeader(RequestLogFilter.REQUEST_ID_HEADER, "trace-1");
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                seen.set(MDC.get(RequestLogFilter.REQUEST_ID_KEY)));

        assertThat(seen.get()).isEqualTo("trace-1");
    }

    @Test
    @DisplayName("The filter removes the ID after the request")
    void idIsGoneAfterTheRequest() throws Exception {
        filter.doFilter(request, response, new MockFilterChain());

        assertThat(MDC.get(RequestLogFilter.REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("An answer of the server with an error reaches the log")
    void answerWithServerErrorIsLogged() throws Exception {
        filter.doFilter(request, response, chainThatAnswers(500));

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("An answer with an error of the client reaches the log")
    void answerWithClientErrorIsLogged() throws Exception {
        filter.doFilter(request, response, chainThatAnswers(404));

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("An answer that was successful reaches the log")
    void successfulAnswerIsLogged() throws Exception {
        filter.doFilter(request, response, chainThatAnswers(200));

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
