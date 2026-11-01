package com.fintrack.transaction.controller;

import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getByUserId(userId));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllByUserId(@PathVariable Long userId) {
        transactionService.deleteAllByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
