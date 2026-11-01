package com.fintrack.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fintrack.expense.entity.ExpenseParticipant;
import com.fintrack.expense.entity.SharedExpense;
import com.fintrack.expense.entity.SplitType;
import com.fintrack.expense.repository.SharedExpenseRepository;
import com.fintrack.expense.service.ExpenseServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test Suite 4: Net Balances Tests (5 tests)
 * Verifies single-expense and multi-expense net balances, positive/negative direction, and zero-sum invariant.
 */
@ExtendWith(MockitoExtension.class)
class NetBalancesTest {

    @Mock
    private SharedExpenseRepository sharedExpenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("14. A pays 120 for A, B, C equally -> A receives 80, B owes 40, C owes 40")
    void shouldCalculateNetBalanceForSingleEqualExpense() {
        SharedExpense expense = new SharedExpense(
                1L,
                "Dinner",
                new BigDecimal("120.00"),
                SplitType.EQUAL,
                LocalDateTime.now()
        );
        expense.addParticipant(new ExpenseParticipant(1L, new BigDecimal("40.00")));
        expense.addParticipant(new ExpenseParticipant(2L, new BigDecimal("40.00")));
        expense.addParticipant(new ExpenseParticipant(3L, new BigDecimal("40.00")));

        when(sharedExpenseRepository.findAllWithParticipants()).thenReturn(List.of(expense));

        Map<Long, BigDecimal> balances = expenseService.getBalances();

        assertEquals(3, balances.size());
        assertEquals(new BigDecimal("80.00"), balances.get(1L));   // A should receive 80
        assertEquals(new BigDecimal("-40.00"), balances.get(2L));  // B owes 40
        assertEquals(new BigDecimal("-40.00"), balances.get(3L));  // C owes 40

        BigDecimal sum = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("0.00"), sum);
    }

    @Test
    @DisplayName("15. Combined net balances across multiple expenses: B pays back A in second expense")
    void shouldCalculateCombinedNetBalanceAcrossMultipleExpenses() {
        // Expense 1: A (1L) pays 120 for A (1L), B (2L), C (3L)
        SharedExpense expense1 = new SharedExpense(
                1L,
                "Rental Car",
                new BigDecimal("120.00"),
                SplitType.EQUAL,
                LocalDateTime.now()
        );
        expense1.addParticipant(new ExpenseParticipant(1L, new BigDecimal("40.00")));
        expense1.addParticipant(new ExpenseParticipant(2L, new BigDecimal("40.00")));
        expense1.addParticipant(new ExpenseParticipant(3L, new BigDecimal("40.00")));

        // Expense 2: B (2L) pays 60 for A (1L) and B (2L)
        SharedExpense expense2 = new SharedExpense(
                2L,
                "Groceries",
                new BigDecimal("60.00"),
                SplitType.EQUAL,
                LocalDateTime.now()
        );
        expense2.addParticipant(new ExpenseParticipant(1L, new BigDecimal("30.00")));
        expense2.addParticipant(new ExpenseParticipant(2L, new BigDecimal("30.00")));

        when(sharedExpenseRepository.findAllWithParticipants()).thenReturn(List.of(expense1, expense2));

        Map<Long, BigDecimal> balances = expenseService.getBalances();

        // A: +120 - 40 - 30 = +50.00
        // B: -40 + 60 - 30 = -10.00
        // C: -40.00
        assertEquals(new BigDecimal("50.00"), balances.get(1L));
        assertEquals(new BigDecimal("-10.00"), balances.get(2L));
        assertEquals(new BigDecimal("-40.00"), balances.get(3L));

        BigDecimal sum = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("0.00"), sum);
    }

    @Test
    @DisplayName("16. Net balance calculation when creator is not in participants list")
    void shouldCalculateNetBalancesWhenCreatorIsNotAParticipant() {
        // A (1L) pays 100 for B (2L) and C (3L)
        SharedExpense expense = new SharedExpense(
                1L,
                "Hotel Room for Friends",
                new BigDecimal("100.00"),
                SplitType.EQUAL,
                LocalDateTime.now()
        );
        expense.addParticipant(new ExpenseParticipant(2L, new BigDecimal("50.00")));
        expense.addParticipant(new ExpenseParticipant(3L, new BigDecimal("50.00")));

        when(sharedExpenseRepository.findAllWithParticipants()).thenReturn(List.of(expense));

        Map<Long, BigDecimal> balances = expenseService.getBalances();

        assertEquals(new BigDecimal("100.00"), balances.get(1L));  // A receives full 100
        assertEquals(new BigDecimal("-50.00"), balances.get(2L));  // B owes 50
        assertEquals(new BigDecimal("-50.00"), balances.get(3L));  // C owes 50

        BigDecimal sum = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("0.00"), sum);
    }

    @Test
    @DisplayName("17. Verify invariant: sum of all net balances across all users always equals zero")
    void shouldVerifySumOfAllNetBalancesAlwaysEqualsZero() {
        // Expense 1: User 10 pays 200 for 10, 20, 30, 40
        SharedExpense e1 = new SharedExpense(10L, "Outing", new BigDecimal("200.00"), SplitType.EQUAL, LocalDateTime.now());
        e1.addParticipant(new ExpenseParticipant(10L, new BigDecimal("50.00")));
        e1.addParticipant(new ExpenseParticipant(20L, new BigDecimal("50.00")));
        e1.addParticipant(new ExpenseParticipant(30L, new BigDecimal("50.00")));
        e1.addParticipant(new ExpenseParticipant(40L, new BigDecimal("50.00")));

        // Expense 2: User 30 pays 90 custom (10: 30.00, 20: 30.00, 30: 30.00)
        SharedExpense e2 = new SharedExpense(30L, "Supplies", new BigDecimal("90.00"), SplitType.CUSTOM, LocalDateTime.now());
        e2.addParticipant(new ExpenseParticipant(10L, new BigDecimal("30.00")));
        e2.addParticipant(new ExpenseParticipant(20L, new BigDecimal("30.00")));
        e2.addParticipant(new ExpenseParticipant(30L, new BigDecimal("30.00")));

        when(sharedExpenseRepository.findAllWithParticipants()).thenReturn(List.of(e1, e2));

        Map<Long, BigDecimal> balances = expenseService.getBalances();

        BigDecimal sumOfAllBalances = balances.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("0.00"), sumOfAllBalances);
    }

    @Test
    @DisplayName("18. Return empty map when no shared expenses exist")
    void shouldReturnEmptyBalancesWhenNoExpensesExist() {
        when(sharedExpenseRepository.findAllWithParticipants()).thenReturn(List.of());

        Map<Long, BigDecimal> balances = expenseService.getBalances();

        assertNotNull(balances);
        assertTrue(balances.isEmpty());
    }
}
