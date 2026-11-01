package com.fintrack.expense.dto;

import com.fintrack.expense.entity.SharedExpense;
import com.fintrack.expense.entity.SplitType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a shared expense including all calculated participant shares.
 *
 * @param id           surrogate identifier
 * @param creatorId    user ID who paid the full amount upfront
 * @param description  narrative summary
 * @param totalAmount  monetary total normalized to 2 decimal places
 * @param splitType    strategy used (EQUAL or CUSTOM)
 * @param participants breakdown of participants and their owed shares
 * @param createdAt    creation timestamp
 */
public record SharedExpenseResponse(
        Long id,
        Long creatorId,
        String description,
        BigDecimal totalAmount,
        SplitType splitType,
        List<ExpenseParticipantResponse> participants,
        LocalDateTime createdAt
) {

    /**
     * Maps a {@link SharedExpense} entity to its response representation.
     *
     * @param expense the source entity
     * @return populated SharedExpenseResponse
     */
    public static SharedExpenseResponse fromEntity(SharedExpense expense) {
        if (expense == null) {
            return null;
        }

        BigDecimal scaledTotal = expense.getTotalAmount() != null
                ? expense.getTotalAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<ExpenseParticipantResponse> participantResponses = expense.getParticipants() != null
                ? expense.getParticipants().stream().map(ExpenseParticipantResponse::fromEntity).toList()
                : List.of();

        return new SharedExpenseResponse(
                expense.getId(),
                expense.getCreatorId(),
                expense.getDescription(),
                scaledTotal,
                expense.getSplitType(),
                participantResponses,
                expense.getCreatedAt()
        );
    }
}

