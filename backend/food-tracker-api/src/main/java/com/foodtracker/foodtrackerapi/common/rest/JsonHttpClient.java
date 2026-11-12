package com.foodtracker.foodtrackerapi.common.rest;

import java.util.Map;

/**
 * <p>
 * Sends a GET request to an external API and gives the answer as a JSON string.
 * </p>
 *
 * <p>
 * The services that read the external APIs use this interface and not the
 * {@code WebClient} of Spring. A test can then give a stub answer and does not
 * need a network connection.
 * </p>
 *
 * @author Janina Mattes
 */
public interface JsonHttpClient {

    /**
     * Sends a GET request.
     *
     * @param url         address of the API
     * @param queryParams parameters of the request. Use an ordered map, because
     *                    the order of the parameters is part of the address.
     * @return the answer as a JSON string, or an empty string if the call
     *         failed
     */
    String getJson(String url, Map<String, String> queryParams);
}
