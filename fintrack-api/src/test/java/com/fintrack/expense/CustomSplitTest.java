package com.fintrack.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintrack.exception.InvalidExpenseException;
import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ParticipantShareRequest;
import com.fintrack.expense.dto.SharedExpenseResponse;
import com.fintrack.expense.entity.SharedExpense;
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
 * Test Suite 2: Custom Split Tests (4 tests)
 * Verifies custom participant share allocation and sum matching validation.
 */
@ExtendWith(MockitoExtension.class)
class CustomSplitTest {

    @Mock
    private SharedExpenseRepository sharedExpenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("5. Custom split succeeds when individual shares exactly equal total")
    void shouldCreateCustomSplitWhenSharesMatchTotal() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Dinner & Dessert",
                new BigDecimal("150.00"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("50.00")),
                        new ParticipantShareRequest(2L, new BigDecimal("70.00")),
                        new ParticipantShareRequest(3L, new BigDecimal("30.00"))
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.totalAmount());
        assertEquals(new BigDecimal("50.00"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("70.00"), response.participants().get(1).shareAmount());
        assertEquals(new BigDecimal("30.00"), response.participants().get(2).shareAmount());
        verify(sharedExpenseRepository).save(any(SharedExpense.class));
    }

    @Test
    @DisplayName("6. Custom split rejected when shares sum is less than total")
    void shouldRejectCustomSplitWhenSharesSumDoesNotMatchTotal() {
        // Total 100.00 != shares 50.00 + 40.00 (90.00)
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Underallocated",
                new BigDecimal("100.00"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("50.00")),
                        new ParticipantShareRequest(2L, new BigDecimal("40.00"))
                )
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("must equal the total expense amount"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("7. Custom split rejected when shares sum exceeds total")
    void shouldRejectCustomSplitWhenSharesExceedTotal() {
        // Total 100.00 != shares 60.00 + 50.00 (110.00)
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Overallocated",
                new BigDecimal("100.00"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("60.00")),
                        new ParticipantShareRequest(2L, new BigDecimal("50.00"))
                )
        );

        InvalidExpenseException ex = assertThrows(InvalidExpenseException.class, () ->
                expenseService.createExpense(request)
        );

        assertTrue(ex.getMessage().contains("must equal the total expense amount"));
        verify(sharedExpenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("8. Custom split preserves arbitrary 2-decimal fractional shares")
    void shouldSupportUnequalCustomSplitWithDecimalPrecision() {
        // Total 75.25 = 25.10 + 50.15
        CreateExpenseRequest request = new CreateExpenseRequest(
                2L,
                "Hardware supplies",
                new BigDecimal("75.25"),
                SplitType.CUSTOM,
                List.of(
                        new ParticipantShareRequest(1L, new BigDecimal("25.10")),
                        new ParticipantShareRequest(2L, new BigDecimal("50.15"))
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("75.25"), response.totalAmount());
        assertEquals(new BigDecimal("25.10"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("50.15"), response.participants().get(1).shareAmount());
    }
}

