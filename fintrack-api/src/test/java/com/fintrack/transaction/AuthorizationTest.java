package com.fintrack.transaction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintrack.exception.UnauthorizedException;
import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.repository.TransactionRepository;
import com.fintrack.transaction.service.TransactionServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test Suite 5: Authorization Tests (4 tests)
 * Verifies caller ownership validation and prevention of unauthorized access and deletions.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("19. Reject transaction creation when authenticated caller is not the owner")
    void shouldRejectTransactionCreationWhenCallerNotOwner() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                10L,
                new BigDecimal("100.00"),
                "Unauthorized Attempt",
                TransactionType.EXPENSE
        );

        assertThrows(UnauthorizedException.class, () ->
                transactionService.create(request, 99L)
        );

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("20. Reject single transaction retrieval when caller is not the record owner")
    void shouldRejectTransactionRetrievalWhenCallerNotOwner() {
        Transaction transaction = new Transaction(
                10L,
                new BigDecimal("150.00"),
                "Confidential Bill",
                TransactionType.EXPENSE,
                LocalDateTime.now()
        );

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertThrows(UnauthorizedException.class, () ->
                transactionService.getById(1L, 99L)
        );
    }

    @Test
    @DisplayName("21. Reject single transaction deletion when caller is not the owner")
    void shouldRejectTransactionDeletionWhenCallerNotOwner() {
        Transaction transaction = new Transaction(
                10L,
                new BigDecimal("75.00"),
                "Restaurant Tab",
                TransactionType.EXPENSE,
                LocalDateTime.now()
        );

        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        assertThrows(UnauthorizedException.class, () ->
                transactionService.deleteById(5L, 99L)
        );

        verify(transactionRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("22. Reject bulk transaction deletion when caller does not match target user")
    void shouldRejectBulkDeletionWhenCallerNotOwner() {
        assertThrows(UnauthorizedException.class, () ->
                transactionService.deleteAllByUserId(10L, 88L)
        );

        verify(transactionRepository, never()).deleteByUserId(any());
    }
}

