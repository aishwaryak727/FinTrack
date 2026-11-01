package com.fintrack.expense.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO representing an individual user's calculated net balance across all shared expenses.
 *
 * @param userId     the unique user identifier
 * @param netBalance the net monetary balance (positive = money to receive, negative = money owed, zero = settled)
 */
public record UserBalanceResponse(
        Long userId,
        BigDecimal netBalance
) {

    /**
     * Constructs a UserBalanceResponse with scale normalized to 2 decimal places.
     *
     * @param userId     user ID
     * @param netBalance raw net balance
     * @return normalized UserBalanceResponse
     */
    public static UserBalanceResponse of(Long userId, BigDecimal netBalance) {
        BigDecimal scaled = netBalance != null
                ? netBalance.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new UserBalanceResponse(userId, scaled);
    }
}

