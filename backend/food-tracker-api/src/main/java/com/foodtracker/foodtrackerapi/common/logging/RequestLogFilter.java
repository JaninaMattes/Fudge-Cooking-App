package com.foodtracker.foodtrackerapi.common.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * Writes one line for each request and gives each request an ID. The ID stands
 * in every log line of the request, because the filter puts it into the
 * diagnostic context (MDC) of the logger. A reader can therefore follow one
 * request through the log, also when many users work at the same time.
 * </p>
 *
 * <p>
 * The filter reads the ID from the header {@code X-Request-Id} when the client
 * sends one, and it writes the ID into the answer. The Android client sends the
 * same header, so one ID connects the log of the client and the log of the
 * server.
 * </p>
 *
 * <p>
 * The level of the line follows the status code: 5xx is an error, 4xx is a
 * warning and the rest is information.
 * </p>
 *
 * @author Janina Mattes
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    /** Name of the header that holds the ID of the request. */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** Name under which the logger holds the ID of the request. */
    public static final String REQUEST_ID_KEY = "requestId";

    /** Highest number of characters that an ID of a client keeps. */
    static final int MAX_ID_LENGTH = 36;

    /** Number of characters of an ID that the filter makes itself. */
    static final int NEW_ID_LENGTH = 8;

    /** Lowest status code of an answer that reports an error of the server. */
    private static final int SERVER_ERROR = 500;

    /** Lowest status code of an answer that reports an error of the client. */
    private static final int CLIENT_ERROR = 400;

    private static final long NANOS_IN_MILLI = 1_000_000L;

    private static final Logger LOG = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestId = requestIdOf(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - start) / NANOS_IN_MILLI;
            write(request, response.getStatus(), millis);
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    /**
     * Reads the ID of the request out of the header, or makes a new one.
     *
     * <p>
     * The method keeps only letters, numbers, the minus and the underscore of
     * an ID of a client. A client could otherwise write its own line into the
     * log with a line break inside the header.
     * </p>
     *
     * @param request request of the client
     * @return the ID of the request
     */
    static String requestIdOf(HttpServletRequest request) {
        String given = request.getHeader(REQUEST_ID_HEADER);
        if (given == null) {
            return newId();
        }
        String clean = given.replaceAll("[^A-Za-z0-9_-]", "");
        if (clean.isEmpty()) {
            return newId();
        }
        return clean.length() > MAX_ID_LENGTH ? clean.substring(0, MAX_ID_LENGTH) : clean;
    }

    private static String newId() {
        return UUID.randomUUID().toString().substring(0, NEW_ID_LENGTH);
    }

    private void write(HttpServletRequest request, int status, long millis) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (status >= SERVER_ERROR) {
            LOG.error("{} {} answered {} after {} ms", method, path, status, millis);
        } else if (status >= CLIENT_ERROR) {
            LOG.warn("{} {} answered {} after {} ms", method, path, status, millis);
        } else {
            LOG.info("{} {} answered {} after {} ms", method, path, status, millis);
        }
    }
}
