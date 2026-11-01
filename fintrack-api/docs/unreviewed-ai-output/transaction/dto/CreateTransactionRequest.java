package com.fintrack.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Type is required")
        String type
) {
}
