package com.finflow.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    private static final String TOPIC = "transaction-events";

    public void publishTransactionEvent(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(TOPIC,
                        String.valueOf(event.getUserId()), // key = userId (same user → same partition)
                        event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction event for user {}: {}",
                        event.getUserId(), ex.getMessage());
            } else {
                log.info("Published [{}] event for transaction {} to partition {}",
                        event.getEventType(),
                        event.getTransactionId(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}