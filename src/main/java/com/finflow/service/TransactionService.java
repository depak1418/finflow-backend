package com.finflow.service;

import com.finflow.dto.PageResponse;
import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.Category;
import com.finflow.entity.Transaction;
import com.finflow.entity.User;
import com.finflow.kafka.TransactionEvent;
import com.finflow.kafka.TransactionEventProducer;
import com.finflow.repository.CategoryRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionEventProducer kafkaProducer;
    private final DashboardService dashboardService;

    // Helper — get logged-in user from security context
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .type(request.getType())
                .category(category)
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // 🔥 Fire Kafka event asynchronously — doesn't slow down the API response
        kafkaProducer.publishTransactionEvent(
                TransactionEvent.builder()
                        .transactionId(saved.getId())
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .amount(saved.getAmount())
                        .description(saved.getDescription())
                        .type(saved.getType())
                        .categoryName(category.getName())
                        .categoryId(category.getId())
                        .transactionDate(saved.getTransactionDate())
                        .eventType("CREATED")
                        .build()
        );

        dashboardService.evictDashboardCache();

        return TransactionResponse.from(saved);
    }

    public PageResponse<TransactionResponse> getTransactions(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());
        Page<TransactionResponse> result = transactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId(), pageable)
                .map(TransactionResponse::from);
        return PageResponse.from(result);
    }

    public TransactionResponse getTransactionById(Long id) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        transaction.setCategory(category);

        Transaction updated = transactionRepository.save(transaction);

        // Fire Kafka event for update too
        kafkaProducer.publishTransactionEvent(
                TransactionEvent.builder()
                        .transactionId(updated.getId())
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .amount(updated.getAmount())
                        .description(updated.getDescription())
                        .type(updated.getType())
                        .categoryName(category.getName())
                        .categoryId(category.getId())
                        .transactionDate(updated.getTransactionDate())
                        .eventType("UPDATED")
                        .build()
        );

        dashboardService.evictDashboardCache();

        return TransactionResponse.from(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Fire Kafka DELETED event before removing
        kafkaProducer.publishTransactionEvent(
                TransactionEvent.builder()
                        .transactionId(transaction.getId())
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .amount(transaction.getAmount())
                        .type(transaction.getType())
                        .categoryName(transaction.getCategory().getName())
                        .categoryId(transaction.getCategory().getId())
                        .transactionDate(transaction.getTransactionDate())
                        .eventType("DELETED")
                        .build()
        );

        dashboardService.evictDashboardCache();

        transactionRepository.delete(transaction);
    }
}