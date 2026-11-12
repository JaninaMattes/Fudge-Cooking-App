package com.foodtracker.foodtrackerapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>
 * The user is not permitted to see the content. Spring sends the status code 401
 * (Unauthorized) to the client when a resource throws this exception.
 * </p>
 *
 * @author Janina Mattes
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class FTAuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Makes the exception.
     *
     * @param message text that tells why the user is not permitted
     */
    public FTAuthException(String message) {
        super(message);
    }
}
