package com.fintrack.exception;

/**
 * Thrown when a requested financial resource cannot be found in the system.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining which resource was missing
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

