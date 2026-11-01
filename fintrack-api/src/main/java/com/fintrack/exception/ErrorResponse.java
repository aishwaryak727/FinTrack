package com.fintrack.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized API error payload for all HTTP responses emitted by FinTrack.
 *
 * @param timestamp       the exact time the error occurred
 * @param status          the HTTP status code
 * @param error           the short error title
 * @param message         human-readable explanation of the error
 * @param path            the request URI that triggered the failure
 * @param validationErrors optional field-level validation error mappings
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {

    /**
     * Factory helper for standard non-validation errors.
     *
     * @param status  the HTTP status code
     * @param error   the error name
     * @param message description of the issue
     * @param path    the request URI
     * @return a structured ErrorResponse instance
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    /**
     * Factory helper for field validation errors.
     *
     * @param status           the HTTP status code
     * @param error            the error name
     * @param message          description of the validation failure
     * @param path             the request URI
     * @param validationErrors map of parameter names to validation messages
     * @return a structured ErrorResponse instance with field errors
     */
    public static ErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}

