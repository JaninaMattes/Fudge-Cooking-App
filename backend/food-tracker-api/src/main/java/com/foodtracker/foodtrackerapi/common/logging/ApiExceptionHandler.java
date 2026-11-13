package com.foodtracker.foodtrackerapi.common.logging;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;

/**
 * <p>
 * Writes an error of an endpoint into the log and gives the client a short
 * answer. The answer holds the ID of the request, so a user can name the ID in
 * a bug report and a reader finds every line of that request in the log.
 * </p>
 *
 * <p>
 * The answer of an error of the server holds no detail of the error. The detail
 * stands in the log, because a message of an exception can hold the name of a
 * table or a part of a statement.
 * </p>
 *
 * @author Janina Mattes
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Text that the client gets when the server fails. */
    static final String SERVER_ERROR_MESSAGE =
            "The server could not answer the request. Please report the ID of the request.";

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Answers a request of a user that is not permitted.
     *
     * @param error error that the endpoint threw
     * @return the status 401 and the reason
     */
    @ExceptionHandler(FTAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthError(FTAuthException error) {
        LOG.warn("The login failed: {}", error.getMessage());
        return answer(HttpStatus.UNAUTHORIZED, error.getMessage());
    }

    /**
     * Answers a request that the server could not finish.
     *
     * @param error error that the endpoint threw
     * @return the status 500 and a short text
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnknownError(Exception error) {
        LOG.error("The request failed: {}", error.getMessage(), error);
        return answer(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR_MESSAGE);
    }

    private static ResponseEntity<Map<String, String>> answer(HttpStatus status, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", String.valueOf(status.value()));
        body.put("message", message == null ? "" : message);
        String requestId = MDC.get(RequestLogFilter.REQUEST_ID_KEY);
        if (requestId != null) {
            body.put(RequestLogFilter.REQUEST_ID_KEY, requestId);
        }
        return ResponseEntity.status(status).body(body);
    }
}
