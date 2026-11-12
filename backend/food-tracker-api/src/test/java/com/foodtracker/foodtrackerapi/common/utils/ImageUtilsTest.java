package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ImageUtils}.
 *
 * @author Janina Mattes
 */
class ImageUtilsTest {

    @Test
    @DisplayName("bytesToBase64 converts the bytes into a Base64 string")
    void bytesToBase64Converts() {
        byte[] bytes = "FeedMe".getBytes(StandardCharsets.UTF_8);

        assertThat(ImageUtils.bytesToBase64(bytes)).isEqualTo("RmVlZE1l");
    }

    @Test
    @DisplayName("bytesToBase64 gives null when there are no bytes")
    void bytesToBase64GivesNullForEmptyInput() {
        assertThat(ImageUtils.bytesToBase64(null)).isNull();
        assertThat(ImageUtils.bytesToBase64(new byte[0])).isNull();
    }

    @Test
    @DisplayName("base64ToBytes converts the Base64 string into the bytes")
    void base64ToBytesConverts() {
        byte[] result = ImageUtils.base64ToBytes("RmVlZE1l");

        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("FeedMe");
    }

    @Test
    @DisplayName("base64ToBytes accepts a Base64 string with line breaks")
    void base64ToBytesAcceptsLineBreaks() {
        byte[] result = ImageUtils.base64ToBytes("RmVl\nZE1l");

        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("FeedMe");
    }

    @Test
    @DisplayName("base64ToBytes gives null when there is no string")
    void base64ToBytesGivesNullForEmptyInput() {
        assertThat(ImageUtils.base64ToBytes(null)).isNull();
        assertThat(ImageUtils.base64ToBytes("")).isNull();
    }

    @Test
    @DisplayName("base64ToBytes ignores the characters that are not Base64")
    void base64ToBytesIgnoresInvalidCharacters() {
        // The MIME decoder accepts text with line breaks, so it ignores a
        // character that does not belong to the Base64 alphabet.
        assertThat(ImageUtils.base64ToBytes("!!!")).isEmpty();
        assertThat(ImageUtils.base64ToBytes("Rm!!Vl\r\nZE1l")).isNotEmpty();
    }

    @Test
    @DisplayName("base64ToBytes gives null for a Base64 string that is not complete")
    void base64ToBytesGivesNullForTruncatedInput() {
        // One single character cannot be a Base64 group, so the decoder throws.
        assertThat(ImageUtils.base64ToBytes("A")).isNull();
    }

    @Test
    @DisplayName("The conversion in both directions keeps the bytes")
    void roundTripKeepsBytes() {
        byte[] bytes = new byte[] { 0, 1, 2, 3, (byte) 254, (byte) 255 };

        byte[] result = ImageUtils.base64ToBytes(ImageUtils.bytesToBase64(bytes));

        assertThat(result).isEqualTo(bytes);
    }
}
