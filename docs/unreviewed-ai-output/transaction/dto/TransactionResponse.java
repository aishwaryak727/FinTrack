package com.fintrack.transaction.dto;

import com.fintrack.transaction.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        String description,
        String type,
        LocalDateTime createdAt
) {

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getType(),
                transaction.getCreatedAt()
        );
    }
}
