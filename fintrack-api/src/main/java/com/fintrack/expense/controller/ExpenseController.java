package com.fintrack.expense.controller;

import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.SharedExpenseResponse;
import com.fintrack.expense.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for creating shared expenses and
 * calculating cumulative net balances across all participants.
 */
@RestController
@RequestMapping("/api/expenses")
@Validated
public class ExpenseController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    /**
     * Constructs ExpenseController with constructor injection.
     *
     * @param expenseService business service managing shared expenses
     */
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Creates and records a new shared expense with EQUAL or CUSTOM split distribution.
     *
     * @param request the validated expense creation payload
     * @return the created shared expense details with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<SharedExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        log.info("REST POST /api/expenses initiated by creatorId={} for amount={}",
                request.creatorId(), request.totalAmount());
        SharedExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the net balances across all recorded expenses for all participating users.
     * Positive balance indicates money to be received; negative indicates money owed.
     *
     * @return map of user ID to net monetary balance with HTTP status 200 (OK)
     */
    @GetMapping("/balances")
    public ResponseEntity<Map<Long, BigDecimal>> getBalances() {
        log.info("REST GET /api/expenses/balances requested");
        Map<Long, BigDecimal> balances = expenseService.getBalances();
        return ResponseEntity.ok(balances);
    }

    /**
     * Retrieves a shared expense by its unique identifier.
     *
     * @param id the unique expense identifier
     * @return the shared expense response with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SharedExpenseResponse> getExpenseById(
            @PathVariable @Positive(message = "Expense ID must be positive") Long id
    ) {
        log.info("REST GET /api/expenses/{} requested", id);
        SharedExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all recorded shared expenses.
     *
     * @return list of all shared expenses with HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<SharedExpenseResponse>> getAllExpenses() {
        log.info("REST GET /api/expenses requested");
        List<SharedExpenseResponse> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }
}

