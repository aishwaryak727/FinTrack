package com.fintrack.expense.dto;

import com.fintrack.expense.entity.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload for creating and splitting a shared group expense.
 *
 * @param creatorId    the ID of the user who paid the full expense amount initially
 * @param description  brief narrative explaining the shared expense
 * @param totalAmount  the total monetary value of the expense (must be > 0, max 2 decimals)
 * @param splitType    the strategy used to divide the expense (EQUAL or CUSTOM)
 * @param participants list of participating users and their designated shares (minimum 2 unique)
 */
public record CreateExpenseRequest(
        @NotNull(message = "Creator ID is required")
        @Positive(message = "Creator ID must be positive")
        Long creatorId,

        @NotBlank(message = "Description is required")
        @Size(min = 1, max = 255, message = "Description must be between 1 and 255 characters")
        String description,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
        @DecimalMax(value = "1000000000.00", message = "Total amount exceeds maximum limit")
        @Digits(integer = 15, fraction = 2, message = "Total amount must have at most 2 decimal places")
        BigDecimal totalAmount,

        @NotNull(message = "Split type is required (EQUAL or CUSTOM)")
        SplitType splitType,

        @NotEmpty(message = "At least two participants are required")
        @Valid
        List<ParticipantShareRequest> participants
) {
}

