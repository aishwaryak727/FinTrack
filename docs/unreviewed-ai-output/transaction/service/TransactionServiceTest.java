package com.fintrack.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateGetAndDeleteTransactionsForAUser() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                42L,
                new BigDecimal("125.50"),
                "Groceries",
                "EXPENSE"
        );

        TransactionResponse created = transactionService.create(request);

        assertNotNull(created.id());
        assertEquals(42L, created.userId());
        assertEquals(new BigDecimal("125.50"), created.amount());

        List<TransactionResponse> userTransactions = transactionService.getByUserId(42L);
        assertEquals(1, userTransactions.size());
        assertFalse(userTransactions.isEmpty());

        transactionService.deleteAllByUserId(42L);

        assertEquals(0, transactionService.getByUserId(42L).size());
    }
}
