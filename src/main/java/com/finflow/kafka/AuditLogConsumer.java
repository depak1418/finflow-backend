package com.finflow.kafka;

import com.finflow.entity.AuditLog;
import com.finflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogConsumer {

    private final AuditLogRepository auditLogRepository;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "audit-log-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        log.info("AuditLogConsumer: logging [{}] event for transaction {}",
                event.getEventType(), event.getTransactionId());

        auditLogRepository.save(
                AuditLog.builder()
                        .transactionId(event.getTransactionId())
                        .userId(event.getUserId())
                        .userEmail(event.getUserEmail())
                        .amount(event.getAmount())
                        .transactionType(event.getType() != null
                                ? event.getType().name() : "UNKNOWN")
                        .categoryName(event.getCategoryName())
                        .eventType(event.getEventType())
                        .transactionDate(event.getTransactionDate())
                        .loggedAt(LocalDateTime.now())
                        .build()
        );
    }
}