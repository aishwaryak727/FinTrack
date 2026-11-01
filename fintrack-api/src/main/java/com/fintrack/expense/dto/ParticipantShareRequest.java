package com.fintrack.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO specifying an individual participant and their optional custom share amount.
 *
 * @param userId      the unique identifier of the participating user
 * @param shareAmount the explicit monetary share (required for CUSTOM split; optional/ignored for EQUAL split)
 */
public record ParticipantShareRequest(
        @NotNull(message = "Participant user ID is required")
        @Positive(message = "Participant user ID must be positive")
        Long userId,

        @DecimalMin(value = "0.01", inclusive = true, message = "Participant share amount must be greater than zero")
        @Digits(integer = 15, fraction = 2, message = "Participant share amount must have at most 2 decimal places")
        BigDecimal shareAmount
) {

    /**
     * Convenience constructor for equal split participant where share amount is omitted.
     *
     * @param userId participant user ID
     */
    public ParticipantShareRequest(Long userId) {
        this(userId, null);
    }
}

