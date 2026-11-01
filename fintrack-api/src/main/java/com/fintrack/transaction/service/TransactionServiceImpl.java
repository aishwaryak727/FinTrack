package com.fintrack.transaction.service;

import com.fintrack.exception.InvalidTransactionException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.exception.UnauthorizedException;
import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production implementation of {@link TransactionService} with strict fintech
 * safeguards including ownership authorization checks, monetary scale normalization,
 * structured logging, and transactional guarantees.
 */
@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;

    /**
     * Constructs a new TransactionServiceImpl with constructor injection.
     *
     * @param transactionRepository the Spring Data JPA transaction repository
     */
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request, Long authenticatedUserId) {
        validateRequest(request);

        if (authenticatedUserId != null && !authenticatedUserId.equals(request.userId())) {
            log.warn("Ownership violation on create: Authenticated caller {} cannot create transactions for user {}",
                    authenticatedUserId, request.userId());
            throw new UnauthorizedException("Cannot create a transaction for another user");
        }

        BigDecimal scaledAmount = request.amount().setScale(2, RoundingMode.HALF_UP);

        log.info("Creating transaction for user {}: amount={}, type={}, description='{}'",
                request.userId(), scaledAmount, request.type(), request.description());

        Transaction transaction = new Transaction(
                request.userId(),
                scaledAmount,
                request.description().trim(),
                request.type(),
                LocalDateTime.now()
        );

        Transaction saved = transactionRepository.save(transaction);
        log.info("Successfully persisted transaction with id={} for user={}", saved.getId(), saved.getUserId());

        return TransactionResponse.fromEntity(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        return create(request, request != null ? request.userId() : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponse getById(Long id, Long authenticatedUserId) {
        if (id == null || id <= 0) {
            throw new InvalidTransactionException("Transaction ID must be positive");
        }

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (authenticatedUserId != null && !authenticatedUserId.equals(transaction.getUserId())) {
            log.warn("Unauthorized access: Caller {} attempted to view transaction {} owned by user {}",
                    authenticatedUserId, id, transaction.getUserId());
            throw new UnauthorizedException("Access denied: You do not have permission to view this transaction");
        }

        log.debug("Retrieved transaction id={} for user={}", id, transaction.getUserId());
        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TransactionResponse> getByUserId(Long userId, Long authenticatedUserId) {
        if (userId == null || userId <= 0) {
            throw new InvalidTransactionException("User ID must be positive");
        }

        if (authenticatedUserId != null && !authenticatedUserId.equals(userId)) {
            log.warn("Unauthorized access: Caller {} attempted to view transactions for user {}",
                    authenticatedUserId, userId);
            throw new UnauthorizedException("Access denied: You do not have permission to view transactions for user " + userId);
        }

        log.debug("Fetching transactions for user {}", userId);
        List<TransactionResponse> results = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();

        log.info("Fetched {} transactions for user {}", results.size(), userId);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TransactionResponse> getByUserId(Long userId) {
        return getByUserId(userId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteById(Long id, Long authenticatedUserId) {
        if (id == null || id <= 0) {
            throw new InvalidTransactionException("Transaction ID must be positive");
        }

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        if (authenticatedUserId != null && !authenticatedUserId.equals(transaction.getUserId())) {
            log.warn("Unauthorized delete: Caller {} attempted to delete transaction {} owned by user {}",
                    authenticatedUserId, id, transaction.getUserId());
            throw new UnauthorizedException("Access denied: You do not have permission to delete this transaction");
        }

        log.info("Deleting transaction id={} owned by user={}", id, transaction.getUserId());
        transactionRepository.deleteByIdAndUserId(id, transaction.getUserId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllByUserId(Long userId, Long authenticatedUserId) {
        if (userId == null || userId <= 0) {
            throw new InvalidTransactionException("User ID must be positive");
        }

        if (authenticatedUserId != null && !authenticatedUserId.equals(userId)) {
            log.warn("Unauthorized bulk delete: Caller {} attempted to delete transactions for user {}",
                    authenticatedUserId, userId);
            throw new UnauthorizedException("Access denied: You do not have permission to delete transactions for user " + userId);
        }

        log.warn("Executing bulk delete of all transactions for user {}", userId);
        transactionRepository.deleteByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        deleteAllByUserId(userId, userId);
    }

    private void validateRequest(CreateTransactionRequest request) {
        if (request == null) {
            throw new InvalidTransactionException("Request body is required");
        }

        if (request.userId() == null || request.userId() <= 0) {
            throw new InvalidTransactionException("User ID must be a positive integer");
        }

        if (request.amount() == null) {
            throw new InvalidTransactionException("Amount is required");
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }

        if (request.amount().scale() > 2) {
            throw new InvalidTransactionException("Amount cannot have more than 2 decimal places");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new InvalidTransactionException("Description is required");
        }

        if (request.description().length() > 255) {
            throw new InvalidTransactionException("Description cannot exceed 255 characters");
        }

        if (request.type() == null) {
            throw new InvalidTransactionException("Transaction type is required");
        }
    }
}
