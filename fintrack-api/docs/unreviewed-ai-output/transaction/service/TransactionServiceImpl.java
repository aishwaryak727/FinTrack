package com.fintrack.transaction.service;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        validateRequest(request);

        Transaction transaction = new Transaction(
                request.userId(),
                request.amount(),
                request.description().trim(),
                request.type().trim(),
                LocalDateTime.now()
        );

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    public List<TransactionResponse> getByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be positive");
        }

        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be positive");
        }

        transactionRepository.deleteByUserId(userId);
    }

    private void validateRequest(CreateTransactionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (request.userId() == null || request.userId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be positive");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }

        if (request.type() == null || request.type().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
        }
    }
}
