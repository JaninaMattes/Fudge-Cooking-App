package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Attempt}.
 *
 * @author Janina Mattes
 */
class AttemptTest {

    @Test
    @DisplayName("orDefault gives the result of an operation that is successful")
    void orDefaultGivesResult() {
        String result = Attempt.orDefault("read a value", "fallback", () -> "value");

        assertThat(result).isEqualTo("value");
    }

    @Test
    @DisplayName("orDefault gives the fallback value when the operation throws")
    void orDefaultGivesFallbackOnException() {
        String result = Attempt.orDefault("read a value", "fallback", () -> {
            throw new IOException("the connection is not available");
        });

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    @DisplayName("orDefault gives the fallback value when the operation gives null")
    void orDefaultGivesFallbackOnNull() {
        String result = Attempt.orDefault("read a value", "fallback", () -> null);

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    @DisplayName("orDefault permits null as the fallback value")
    void orDefaultPermitsNullFallback() {
        String result = Attempt.orDefault("read a value", null, () -> {
            throw new IllegalStateException("no value");
        });

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("run gives true when the operation is successful")
    void runGivesTrueOnSuccess() {
        AtomicBoolean operationRan = new AtomicBoolean(false);

        boolean result = Attempt.run("write a value", () -> operationRan.set(true));

        assertThat(result).isTrue();
        assertThat(operationRan).isTrue();
    }

    @Test
    @DisplayName("run gives false when the operation throws")
    void runGivesFalseOnException() {
        boolean result = Attempt.run("write a value", () -> {
            throw new IllegalArgumentException("the value is not valid");
        });

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("run accepts an exception without a message")
    void runAcceptsExceptionWithoutMessage() {
        boolean result = Attempt.run("write a value", () -> {
            throw new NullPointerException();
        });

        assertThat(result).isFalse();
    }
}
