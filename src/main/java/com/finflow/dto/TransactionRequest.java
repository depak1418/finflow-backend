package com.finflow.dto;

import com.finflow.entity.Transaction;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type; // INCOME or EXPENSE

    @NotNull(message = "Category is required")
    private Long categoryId;
}