package com.fintrack.transaction.controller;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for financial transaction management.
 * Enforces request body validation, path variable validation, and caller authorization checks.
 */
@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    /**
     * Constructs a new TransactionController with constructor injection.
     *
     * @param transactionService the transaction business service
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new financial transaction for a user account.
     *
     * @param request             the validated transaction creation payload
     * @param authenticatedUserId the optional caller ID supplied in the {@code X-User-Id} header
     * @return the created transaction details with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId
    ) {
        log.info("REST POST /api/transactions for target userId={}, caller={}", request.userId(), authenticatedUserId);
        TransactionResponse response = transactionService.create(request, authenticatedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a single transaction by its unique identifier.
     *
     * @param id                  the transaction ID (must be positive)
     * @param authenticatedUserId the optional caller ID supplied in the {@code X-User-Id} header
     * @return the transaction response with HTTP status 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable @Positive(message = "Transaction ID must be positive") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId
    ) {
        log.info("REST GET /api/transactions/{} requested by caller={}", id, authenticatedUserId);
        TransactionResponse response = transactionService.getById(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all transactions belonging to a specific user, ordered with newest first.
     *
     * @param userId              the target user identifier (must be positive)
     * @param authenticatedUserId the optional caller ID supplied in the {@code X-User-Id} header
     * @return list of transaction responses with HTTP status 200 (OK)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> getByUserId(
            @PathVariable @Positive(message = "User ID must be positive") Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId
    ) {
        log.info("REST GET /api/transactions/user/{} requested by caller={}", userId, authenticatedUserId);
        List<TransactionResponse> responses = transactionService.getByUserId(
                userId,
                authenticatedUserId != null ? authenticatedUserId : userId
        );
        return ResponseEntity.ok(responses);
    }

    /**
     * Deletes a specific transaction by its unique identifier.
     *
     * @param id                  the transaction ID (must be positive)
     * @param authenticatedUserId the optional caller ID supplied in the {@code X-User-Id} header
     * @return HTTP status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @Positive(message = "Transaction ID must be positive") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId
    ) {
        log.info("REST DELETE /api/transactions/{} requested by caller={}", id, authenticatedUserId);
        transactionService.deleteById(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all transactions belonging to a specific user.
     *
     * @param userId              the user identifier whose transactions will be purged
     * @param authenticatedUserId the optional caller ID supplied in the {@code X-User-Id} header
     * @return HTTP status 204 (No Content)
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllByUserId(
            @PathVariable @Positive(message = "User ID must be positive") Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long authenticatedUserId
    ) {
        log.warn("REST DELETE /api/transactions/user/{} requested by caller={}", userId, authenticatedUserId);
        transactionService.deleteAllByUserId(
                userId,
                authenticatedUserId != null ? authenticatedUserId : userId
        );
        return ResponseEntity.noContent().build();
    }
}
