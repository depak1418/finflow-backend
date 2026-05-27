package com.finflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String transactionType;  // INCOME / EXPENSE
    private String categoryName;
    private String eventType;        // CREATED / UPDATED / DELETED
    private LocalDate transactionDate;
    private LocalDateTime loggedAt;
}