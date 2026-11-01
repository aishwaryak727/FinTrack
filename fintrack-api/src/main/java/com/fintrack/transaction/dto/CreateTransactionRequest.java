package com.fintrack.transaction.dto;

import com.fintrack.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request payload for creating a new financial transaction.
 *
 * @param userId      the identifier of the account owner
 * @param amount      the monetary value (must be >= 0.01, <= 1,000,000,000.00, max 2 decimals)
 * @param description brief narrative explaining the transaction (1 to 255 characters)
 * @param type        the categorized transaction type (INCOME, EXPENSE, or TRANSFER)
 */
public record CreateTransactionRequest(
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
        @DecimalMax(value = "1000000000.00", message = "Amount exceeds allowable transaction limit")
        @Digits(integer = 15, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        @Size(min = 1, max = 255, message = "Description must be between 1 and 255 characters")
        String description,

        @NotNull(message = "Transaction type is required (e.g., INCOME, EXPENSE, TRANSFER)")
        TransactionType type
) {
}
