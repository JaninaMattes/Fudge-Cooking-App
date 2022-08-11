package com.foodtracker.foodtrackerapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to throw if user is unauthorized to access content.
 * 
 * @author Janina Mattes
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class FTAuthException extends RuntimeException {

    public FTAuthException(String message) {
        super(message);
        }
}