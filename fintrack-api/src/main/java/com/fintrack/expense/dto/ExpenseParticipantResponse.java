package com.fintrack.expense.dto;

import com.fintrack.expense.entity.ExpenseParticipant;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Response DTO representing an individual participant in a shared expense.
 *
 * @param id          surrogate participant identifier
 * @param userId      user ID of the participant
 * @param shareAmount normalized monetary share owed by this participant
 */
public record ExpenseParticipantResponse(
        Long id,
        Long userId,
        BigDecimal shareAmount
) {

    /**
     * Maps an {@link ExpenseParticipant} entity to its response DTO.
     *
     * @param participant the entity to convert
     * @return populated ExpenseParticipantResponse
     */
    public static ExpenseParticipantResponse fromEntity(ExpenseParticipant participant) {
        if (participant == null) {
            return null;
        }

        BigDecimal scaledShare = participant.getShareAmount() != null
                ? participant.getShareAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return new ExpenseParticipantResponse(
                participant.getId(),
                participant.getUserId(),
                scaledShare
        );
    }
}

