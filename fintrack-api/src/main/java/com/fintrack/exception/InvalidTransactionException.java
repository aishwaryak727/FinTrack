package com.fintrack.exception;

/**
 * Thrown when transaction parameters, monetary values, or operational rules
 * violate financial domain invariants.
 */
public class InvalidTransactionException extends RuntimeException {

    /**
     * Constructs a new InvalidTransactionException with the specified detail message.
     *
     * @param message the detail message describing the validation failure
     */
    public InvalidTransactionException(String message) {
        super(message);
    }
}

