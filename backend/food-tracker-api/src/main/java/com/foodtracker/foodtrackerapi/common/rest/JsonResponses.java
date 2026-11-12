package com.foodtracker.foodtrackerapi.common.rest;

/**
 * <p>
 * Tests the answer of an external API before the application reads it.
 * </p>
 *
 * @author Janina Mattes
 */
public final class JsonResponses {

    /**
     * Shortest answer that can hold data. A shorter answer is for example
     * {@code {}} or {@code []}.
     */
    private static final int MIN_LENGTH = 5;

    private JsonResponses() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Tells if the answer of an external API can hold data. An answer that is
     * empty, too short, or that holds the word "error", holds no data.
     *
     * @param jsonResponse answer of the API. It can be {@code null}.
     * @return {@code true} if the answer can hold data
     */
    public static boolean holdsData(String jsonResponse) {
        return jsonResponse != null
                && jsonResponse.length() >= MIN_LENGTH
                && !jsonResponse.contains("error");
    }
}
