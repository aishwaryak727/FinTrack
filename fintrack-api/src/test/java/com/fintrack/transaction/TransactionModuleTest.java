package com.fintrack.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.transaction.service.TransactionServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test Suite 6: Transaction Module Tests (4 tests)
 * Verifies transaction creation, retrieval (single & list), scale normalization, and deletion.
 */
@ExtendWith(MockitoExtension.class)
class TransactionModuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("23. Create transaction successfully with 2-decimal scale normalization")
    void shouldCreateTransactionSuccessfullyWithScaleNormalization() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                10L,
                new BigDecimal("49.9"),
                "Coffee & Pastry",
                TransactionType.EXPENSE
        );

        Transaction savedEntity = new Transaction(
                10L,
                new BigDecimal("49.90"),
                "Coffee & Pastry",
                TransactionType.EXPENSE,
                LocalDateTime.now()
        );

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedEntity);

        TransactionResponse response = transactionService.create(request, 10L);

        assertNotNull(response);
        assertEquals(10L, response.userId());
        assertEquals(new BigDecimal("49.90"), response.amount());
        assertEquals(TransactionType.EXPENSE, response.type());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(new BigDecimal("49.90"), captor.getValue().getAmount());
    }

    @Test
    @DisplayName("24. Retrieve single transaction by ID for authorized owner")
    void shouldRetrieveTransactionByIdForAuthorizedOwner() {
        Transaction transaction = new Transaction(
                10L,
                new BigDecimal("150.00"),
                "Electric bill",
                TransactionType.EXPENSE,
                LocalDateTime.now()
        );

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getById(1L, 10L);

        assertNotNull(response);
        assertEquals(10L, response.userId());
        assertEquals(new BigDecimal("150.00"), response.amount());
        assertEquals("Electric bill", response.description());
    }

    @Test
    @DisplayName("25. Retrieve all transactions for authorized owner ordered by newest first")
    void shouldRetrieveAllTransactionsByUserIdForAuthorizedOwner() {
        Transaction t1 = new Transaction(10L, new BigDecimal("20.00"), "Lunch", TransactionType.EXPENSE, LocalDateTime.now());
        Transaction t2 = new Transaction(10L, new BigDecimal("500.00"), "Salary", TransactionType.INCOME, LocalDateTime.now());

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(t1, t2));

        List<TransactionResponse> responses = transactionService.getByUserId(10L, 10L);

        assertEquals(2, responses.size());
        assertEquals(new BigDecimal("20.00"), responses.get(0).amount());
        assertEquals(new BigDecimal("500.00"), responses.get(1).amount());
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    @DisplayName("26. Delete transaction by ID for authorized owner")
    void shouldDeleteTransactionByIdForAuthorizedOwner() {
        Transaction transaction = new Transaction(
                10L,
                new BigDecimal("75.00"),
                "Dinner",
                TransactionType.EXPENSE,
                LocalDateTime.now()
        );

        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        transactionService.deleteById(5L, 10L);

        verify(transactionRepository).deleteByIdAndUserId(5L, 10L);
    }
}

