package com.finflow.kafka;

import com.finflow.entity.Transaction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private Long transactionId;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String description;
    private Transaction.TransactionType type;
    private String categoryName;
    private Long categoryId;
    private LocalDate transactionDate;
    private String eventType; // CREATED, UPDATED, DELETED
}