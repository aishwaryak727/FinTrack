package com.fintrack.expense.service;

import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.SharedExpenseResponse;
import com.fintrack.expense.dto.UserBalanceResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service contract defining operations for creating shared expenses,
 * validating split rules, and computing net balances across all group participants.
 */
public interface ExpenseService {

    /**
     * Creates and persists a shared expense with calculated or validated participant shares.
     *
     * @param request the validated expense creation payload
     * @return the created shared expense details
     */
    SharedExpenseResponse createExpense(CreateExpenseRequest request);

    /**
     * Retrieves a shared expense by its unique identifier.
     *
     * @param id the expense ID
     * @return the shared expense response
     */
    SharedExpenseResponse getExpenseById(Long id);

    /**
     * Retrieves all shared expenses recorded in the system.
     *
     * @return list of all shared expenses
     */
    List<SharedExpenseResponse> getAllExpenses();

    /**
     * Calculates the net balances across all recorded expenses for all users.
     * Positive balance indicates money the user should receive.
     * Negative balance indicates money the user owes.
     * The sum of all net balances across all users equals zero.
     *
     * @return map of user ID to their net monetary balance (scaled to 2 decimal places)
     */
    Map<Long, BigDecimal> getBalances();

    /**
     * Calculates the net balances across all recorded expenses returning a list of DTOs.
     *
     * @return list of user balance responses
     */
    List<UserBalanceResponse> getUserBalances();
}

