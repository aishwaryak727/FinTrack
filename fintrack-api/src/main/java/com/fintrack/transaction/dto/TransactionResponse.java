package com.fintrack.transaction.dto;

import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Immutable response representation of a financial transaction.
 *
 * @param id          surrogate database identifier
 * @param userId      user who owns this transaction
 * @param amount      monetary amount normalized to 2 decimal places
 * @param description narrative summary
 * @param type        the categorized transaction type
 * @param createdAt   audit timestamp when the transaction was recorded
 */
public record TransactionResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        String description,
        TransactionType type,
        LocalDateTime createdAt
) {

    /**
     * Converts a JPA {@link Transaction} entity into an immutable {@link TransactionResponse} DTO.
     *
     * @param transaction the source transaction entity
     * @return a populated TransactionResponse instance with normalized monetary scale
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        BigDecimal scaledAmount = transaction.getAmount() != null
                ? transaction.getAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                scaledAmount,
                transaction.getDescription(),
                transaction.getType(),
                transaction.getCreatedAt()
        );
    }
}
