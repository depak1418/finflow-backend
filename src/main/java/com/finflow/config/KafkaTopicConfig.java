package com.finflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name("transaction-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic budgetAlertsTopic() {
        return TopicBuilder.name("budget-alerts")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditLogsTopic() {
        return TopicBuilder.name("audit-logs")
                .partitions(1)
                .replicas(1)
                .build();
    }

//    @Bean
//    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
//        return new KafkaTemplate<>(producerFactory);
//    }
}