package com.fintrack.transaction.service;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import java.util.List;

public interface TransactionService {

    TransactionResponse create(CreateTransactionRequest request);

    List<TransactionResponse> getByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
