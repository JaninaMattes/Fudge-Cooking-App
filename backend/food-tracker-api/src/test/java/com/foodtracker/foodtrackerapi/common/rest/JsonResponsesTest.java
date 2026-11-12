package com.foodtracker.foodtrackerapi.common.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link JsonResponses}.
 *
 * @author Janina Mattes
 */
class JsonResponsesTest {

    @ParameterizedTest
    @DisplayName("holdsData gives false for an answer without data")
    @NullAndEmptySource
    @ValueSource(strings = {
            "{}",
            "[]",
            "{\"error\":\"the key is not valid\"}",
            "{\"message\":\"error\"}"
    })
    void holdsDataGivesFalse(String response) {
        assertThat(JsonResponses.holdsData(response)).isFalse();
    }

    @Test
    @DisplayName("holdsData gives true for an answer with data")
    void holdsDataGivesTrue() {
        assertThat(JsonResponses.holdsData("{\"hints\":[]}")).isTrue();
        assertThat(JsonResponses.holdsData("[{\"id\":1}]")).isTrue();
    }
}
