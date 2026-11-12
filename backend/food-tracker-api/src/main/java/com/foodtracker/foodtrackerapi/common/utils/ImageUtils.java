package com.foodtracker.foodtrackerapi.common.utils;

import java.util.Base64;

/**
 * <p>
 * Converts an image between a byte array and a Base64 string.
 * </p>
 *
 * <p>
 * The database stores the image as a byte array. The REST API sends the image as
 * a Base64 string, because JSON holds text only.
 * </p>
 *
 * @author Janina Mattes
 */
public final class ImageUtils {

    private ImageUtils() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Converts a byte array into a Base64 string.
     *
     * @param bytes bytes of the image. It can be {@code null}.
     * @return the Base64 string, or {@code null} if the array is {@code null} or
     *         empty
     */
    public static String bytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Converts a Base64 string into a byte array.
     *
     * @param base64Str Base64 string. It can be {@code null}. Line breaks are
     *                  permitted, because some clients send MIME Base64.
     * @return the bytes of the image, or {@code null} if the string is
     *         {@code null}, empty or not valid Base64
     */
    public static byte[] base64ToBytes(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) {
            return null;
        }
        return Attempt.orDefault("decode the Base64 image", null,
                () -> Base64.getMimeDecoder().decode(base64Str));
    }
}
