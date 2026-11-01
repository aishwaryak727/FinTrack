package com.fintrack.expense;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fintrack.exception.InvalidExpenseException;
import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ParticipantShareRequest;
import com.fintrack.expense.entity.SplitType;
import com.fintrack.expense.repository.SharedExpenseRepository;
import com.fintrack.expense.service.ExpenseServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test Suite 3: Validation Tests (5 tests)
 * Verifies business validations: participant count, uniqueness, zero/negative bounds, and custom shares.
 */
@ExtendWith(MockitoExtension.class)
class ValidationTest {

    @Mock
    private SharedExpenseRepository sharedExpenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("9. Reject expense when only one participant is provided")
    void shouldRejectExpenseWithOnlyOneParticipant() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Solo Expense",
                new BigDecimal("50.00"),
                SplitType.EQUAL,
                List.of(new ParticipantShareRequest(1L))
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("at least two unique participants"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("10. Reject expense when duplicate participants are present")
    void shouldRejectExpenseWithDuplicateParticipants() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Duplicate Entry",
                new BigDecimal("50.00"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(1L)
                )
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("Duplicate participant detected"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("11. Reject expense with zero total amount")
    void shouldRejectExpenseWithZeroTotalAmount() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Zero Amount",
                BigDecimal.ZERO,
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(2L)
                )
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("greater than zero"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("12. Reject expense with negative total amount")
    void shouldRejectExpenseWithNegativeTotalAmount() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Negative Amount",
                new BigDecimal("-25.00"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(2L)
                )
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("greater than zero"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("13. Reject custom split when a participant share is non-positive or null")
    void shouldRejectCustomSplitWithNonPositiveOrMissingShareAmount() {
        CreateExpenseRequest requestWithZeroShare = new CreateExpenseRequest(
                1L,
                "Zero Share",
                new BigDecimal("50.00"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("50.00")),
                        new ParticipantShareRequest(2L, BigDecimal.ZERO)
                )
        );

        assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(requestWithZeroShare)
        );

        CreateExpenseRequest requestWithNullShare = new CreateExpenseRequest(
                1L,
                "Null Share",
                new BigDecimal("50.00"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("50.00")),
                        new ParticipantShareRequest(2L, null)
                )
        );

        assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(requestWithNullShare)
        );

        verify(sharedExpenseRepository, never()).save(any());
    }
}

