package com.finflow.controller;

import com.finflow.dto.PageResponse;
import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a transaction")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    @GetMapping
    @Operation(summary = "Get all transactions (paginated)")
    public ResponseEntity<PageResponse<TransactionResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.getTransactions(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}