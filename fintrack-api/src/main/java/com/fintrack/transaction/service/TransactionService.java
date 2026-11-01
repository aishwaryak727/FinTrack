package com.fintrack.transaction.service;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import java.util.List;

/**
 * Service contract defining core financial transaction operations including
 * creation, retrieval, validation, and authorization management.
 */
public interface TransactionService {

    /**
     * Creates a new financial transaction with caller authorization verification.
     *
     * @param request             the validated transaction creation payload
     * @param authenticatedUserId the ID of the authenticated user performing the operation
     * @return the created transaction details
     */
    TransactionResponse create(CreateTransactionRequest request, Long authenticatedUserId);

    /**
     * Creates a new transaction using the user ID from the request as the authorized caller.
     *
     * @param request the validated transaction creation payload
     * @return the created transaction details
     */
    TransactionResponse create(CreateTransactionRequest request);

    /**
     * Retrieves a single transaction by ID, verifying that the caller owns the transaction.
     *
     * @param id                  the transaction ID
     * @param authenticatedUserId the ID of the authenticated caller
     * @return the transaction response
     */
    TransactionResponse getById(Long id, Long authenticatedUserId);

    /**
     * Retrieves all transactions for a user, verifying caller authorization.
     *
     * @param userId              the owner user ID whose transactions are requested
     * @param authenticatedUserId the ID of the authenticated caller
     * @return list of transaction responses ordered newest first
     */
    List<TransactionResponse> getByUserId(Long userId, Long authenticatedUserId);

    /**
     * Retrieves all transactions for a user.
     *
     * @param userId the owner user ID
     * @return list of transaction responses ordered newest first
     */
    List<TransactionResponse> getByUserId(Long userId);

    /**
     * Deletes a specific transaction by ID, verifying ownership by the caller.
     *
     * @param id                  the transaction ID
     * @param authenticatedUserId the ID of the authenticated caller
     */
    void deleteById(Long id, Long authenticatedUserId);

    /**
     * Deletes all transactions for a user, verifying caller authorization.
     *
     * @param userId              the owner user ID
     * @param authenticatedUserId the ID of the authenticated caller
     */
    void deleteAllByUserId(Long userId, Long authenticatedUserId);

    /**
     * Deletes all transactions for a user.
     *
     * @param userId the owner user ID
     */
    void deleteAllByUserId(Long userId);
}
