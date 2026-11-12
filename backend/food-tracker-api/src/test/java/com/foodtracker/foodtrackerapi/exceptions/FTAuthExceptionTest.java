package com.foodtracker.foodtrackerapi.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Tests for {@link FTAuthException}.
 *
 * @author Janina Mattes
 */
class FTAuthExceptionTest {

    @Test
    @DisplayName("The exception keeps the message")
    void exceptionKeepsMessage() {
        FTAuthException exception = new FTAuthException("Invalid EMAIL/PASSWORD");

        assertThat(exception).hasMessage("Invalid EMAIL/PASSWORD");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("The exception gives the client the status 401")
    void exceptionGivesStatusUnauthorized() {
        ResponseStatus status = FTAuthException.class.getAnnotation(ResponseStatus.class);

        assertThat(status).isNotNull();
        assertThat(status.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
