package com.finflow.dto;

import com.finflow.entity.Transaction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private Transaction.TransactionType type;
    private String categoryName;
    private String categoryIcon;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .type(t.getType())
                .categoryName(t.getCategory().getName())
                .categoryIcon(t.getCategory().getIcon())
                .createdAt(t.getCreatedAt())
                .build();
    }
}