package com.fintrack.exception;

/**
 * Thrown when an authenticated user attempts to access or mutate a resource
 * that belongs to another user without appropriate authorization.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     *
     * @param message the detail message describing the authorization violation
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}

