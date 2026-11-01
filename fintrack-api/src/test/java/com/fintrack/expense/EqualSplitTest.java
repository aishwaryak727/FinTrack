package com.fintrack.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ParticipantShareRequest;
import com.fintrack.expense.dto.SharedExpenseResponse;
import com.fintrack.expense.entity.ExpenseParticipant;
import com.fintrack.expense.entity.SharedExpense;
import com.fintrack.expense.entity.SplitType;
import com.fintrack.expense.repository.SharedExpenseRepository;
import com.fintrack.expense.service.ExpenseServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test Suite 1: Equal Split Tests (4 tests)
 * Verifies equal division among participants and exact penny rounding distribution.
 */
@ExtendWith(MockitoExtension.class)
class EqualSplitTest {

    @Mock
    private SharedExpenseRepository sharedExpenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("1. Equal division among three participants without remainder")
    void shouldSplitEquallyAmongThreeParticipantsWithoutRemainder() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Team Lunch",
                new BigDecimal("120.00"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(2L),
                        new ParticipantShareRequest(3L)
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("120.00"), response.totalAmount());
        assertEquals(3, response.participants().size());
        assertEquals(new BigDecimal("40.00"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("40.00"), response.participants().get(1).shareAmount());
        assertEquals(new BigDecimal("40.00"), response.participants().get(2).shareAmount());

        BigDecimal sumOfShares = response.participants().stream()
                .map(p -> p.shareAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("120.00"), sumOfShares);
    }

    @Test
    @DisplayName("2. Equal division with penny remainder distributed to guarantee exact total")
    void shouldHandleRoundingDifferencesWithPennyDistribution() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Shared Cab",
                new BigDecimal("100.00"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(2L),
                        new ParticipantShareRequest(3L)
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("33.34"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("33.33"), response.participants().get(1).shareAmount());
        assertEquals(new BigDecimal("33.33"), response.participants().get(2).shareAmount());

        BigDecimal sum = response.participants().stream()
                .map(p -> p.shareAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), sum);
    }

    @Test
    @DisplayName("3. Equal split between two participants divides amount evenly")
    void shouldHandleEqualSplitWithEvenTwoParticipants() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                10L,
                "Coffee",
                new BigDecimal("50.00"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(10L),
                        new ParticipantShareRequest(20L)
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(2, response.participants().size());
        assertEquals(new BigDecimal("25.00"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("25.00"), response.participants().get(1).shareAmount());
    }

    @Test
    @DisplayName("4. Equal split across four participants with remainder pennies")
    void shouldHandleEqualSplitWithFourParticipantsAndRemainder() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                1L,
                "Groceries",
                new BigDecimal("10.02"),
                SplitType.EQUAL,
                List.of(
                        new ParticipantShareRequest(1L),
                        new ParticipantShareRequest(2L),
                        new ParticipantShareRequest(3L),
                        new ParticipantShareRequest(4L)
                )
        );

        when(sharedExpenseRepository.save(any(SharedExpense.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response);
        assertEquals(4, response.participants().size());
        assertEquals(new BigDecimal("2.51"), response.participants().get(0).shareAmount());
        assertEquals(new BigDecimal("2.51"), response.participants().get(1).shareAmount());
        assertEquals(new BigDecimal("2.50"), response.participants().get(2).shareAmount());
        assertEquals(new BigDecimal("2.50"), response.participants().get(3).shareAmount());

        ArgumentCaptor<SharedExpense> captor = ArgumentCaptor.forClass(SharedExpense.class);
        verify(sharedExpenseRepository).save(captor.capture());
        BigDecimal totalShareSum = captor.getValue().getParticipants().stream()
                .map(ExpenseParticipant::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("10.02"), totalShareSum);
    }
}

