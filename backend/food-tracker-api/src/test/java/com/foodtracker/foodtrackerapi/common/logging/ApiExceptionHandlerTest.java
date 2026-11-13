package com.foodtracker.foodtrackerapi.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;

/**
 * Tests for {@link ApiExceptionHandler}.
 *
 * @author Janina Mattes
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @AfterEach
    void clearTheContextOfTheLogger() {
        MDC.clear();
    }

    @Test
    @DisplayName("A user that is not permitted gets the status 401 and the reason")
    void userThatIsNotPermittedGets401() {
        ResponseEntity<Map<String, String>> answer =
                handler.handleAuthError(new FTAuthException("Invalid EMAIL/PASSWORD"));

        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(answer.getBody()).containsEntry("status", "401")
                .containsEntry("message", "Invalid EMAIL/PASSWORD");
    }

    @Test
    @DisplayName("An error of the server gives the status 500 and no detail")
    void errorOfTheServerGives500() {
        ResponseEntity<Map<String, String>> answer =
                handler.handleUnknownError(new IllegalStateException("table ET_PRODUCT is gone"));

        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(answer.getBody())
                .containsEntry("message", ApiExceptionHandler.SERVER_ERROR_MESSAGE);
        assertThat(answer.getBody().toString()).doesNotContain("ET_PRODUCT");
    }

    @Test
    @DisplayName("The answer holds the ID of the request")
    void answerHoldsTheIdOfTheRequest() {
        MDC.put(RequestLogFilter.REQUEST_ID_KEY, "trace-7");

        ResponseEntity<Map<String, String>> answer =
                handler.handleUnknownError(new IllegalStateException("broken"));

        assertThat(answer.getBody()).containsEntry(RequestLogFilter.REQUEST_ID_KEY, "trace-7");
    }

    @Test
    @DisplayName("The answer holds no ID when the request has none")
    void answerHoldsNoIdWhenTheRequestHasNone() {
        ResponseEntity<Map<String, String>> answer =
                handler.handleUnknownError(new IllegalStateException("broken"));

        assertThat(answer.getBody()).doesNotContainKey(RequestLogFilter.REQUEST_ID_KEY);
    }

    @Test
    @DisplayName("An error without a message gives an empty text")
    void errorWithoutAMessageGivesAnEmptyText() {
        ResponseEntity<Map<String, String>> answer =
                handler.handleAuthError(new FTAuthException(null));

        assertThat(answer.getBody()).containsEntry("message", "");
    }
}
