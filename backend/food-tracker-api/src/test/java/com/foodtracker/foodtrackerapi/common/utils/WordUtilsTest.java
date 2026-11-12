package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link WordUtils}.
 *
 * @author Janina Mattes
 */
class WordUtilsTest {

    @ParameterizedTest
    @DisplayName("capitalizeString writes the first letter of each word with a capital letter")
    @CsvSource({
            "'fresh basil', 'Fresh Basil'",
            "'FRESH BASIL', 'Fresh Basil'",
            "'french cream cheese', 'French Cream Cheese'",
            "'milk. cream', 'Milk. Cream'",
            "'o''clock tea', 'O''Clock Tea'",
            "'  flour', '  Flour'",
            "'123 grams', '123 Grams'",
            "'', ''"
    })
    void capitalizeStringWritesCapitalLetters(String input, String expected) {
        assertThat(WordUtils.capitalizeString(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("capitalizeString gives null when the text is null")
    void capitalizeStringGivesNullForNull() {
        assertThat(WordUtils.capitalizeString(null)).isNull();
    }

    @ParameterizedTest
    @DisplayName("removeHtmlTags removes the tags and keeps the text")
    @CsvSource({
            "'<b>1562 calories</b>', '1562 calories'",
            "'<p>Mix <i>all</i> parts.</p>', 'Mix all parts.'",
            "'no tags here', 'no tags here'",
            "'', ''"
    })
    void removeHtmlTagsRemovesTags(String input, String expected) {
        assertThat(WordUtils.removeHtmlTags(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("removeHtmlTags gives null when the text is null")
    void removeHtmlTagsGivesNullForNull() {
        assertThat(WordUtils.removeHtmlTags(null)).isNull();
    }
}
