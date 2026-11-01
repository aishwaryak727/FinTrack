package com.fintrack.exception;

/**
 * Thrown when an expense splitting request violates business invariants,
 * such as insufficient participants, duplicate participants, non-positive amounts,
 * or mismatched custom split sums.
 */
public class InvalidExpenseException extends RuntimeException {

    /**
     * Constructs a new InvalidExpenseException with the specified detail message.
     *
     * @param message description of the validation or business rule failure
     */
    public InvalidExpenseException(String message) {
        super(message);
    }
}

