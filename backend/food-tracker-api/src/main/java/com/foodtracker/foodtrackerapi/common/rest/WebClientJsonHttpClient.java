package com.foodtracker.foodtrackerapi.common.rest;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;

/**
 * <p>
 * Sends the GET requests to the external APIs with the {@code WebClient} of
 * Spring.
 * </p>
 *
 * <p>
 * The client waits for the answer, because the REST endpoints of this
 * application are not reactive. A call that takes too long ends with the
 * timeout, and the service then works with an empty answer.
 * </p>
 *
 * @author Janina Mattes
 * @see <a href="https://www.baeldung.com/webflux-webclient-parameters">WebClient
 *      parameters</a>
 */
@Component
public class WebClientJsonHttpClient implements JsonHttpClient {

    /** Longest time to wait for the answer of an external API. */
    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public String getJson(String url, Map<String, String> queryParams) {
        return Attempt.orDefault("read JSON from " + url, "",
                () -> WebClient.create(url)
                        .get()
                        .uri(uriBuilder -> {
                            queryParams.forEach(uriBuilder::queryParam);
                            return uriBuilder.build();
                        })
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(REQUEST_TIMEOUT));
    }
}
