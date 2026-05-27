package com.finflow.kafka;

import com.finflow.entity.Budget;
import com.finflow.entity.Notification;
import com.finflow.entity.Transaction;
import com.finflow.entity.User;
import com.finflow.repository.BudgetRepository;
import com.finflow.repository.NotificationRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertConsumer {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "budget-alert-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        // Only check expenses — income doesn't affect budgets
        if (event.getType() != Transaction.TransactionType.EXPENSE) return;
        if ("DELETED".equals(event.getEventType())) return;

        log.info("BudgetAlertConsumer received event for user {} category {}",
                event.getUserId(), event.getCategoryName());

        LocalDate date = event.getTransactionDate();
        int month = date.getMonthValue();
        int year  = date.getYear();

        // Find budget for this category/month/year
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                        event.getUserId(), event.getCategoryId(), month, year)
                .ifPresent(budget -> checkAndNotify(budget, event, month, year));
    }

    private void checkAndNotify(Budget budget, TransactionEvent event,
                                int month, int year) {
        // Calculate total spent for this category this month
        List<Transaction> txns = transactionRepository
                .findByUserIdAndTransactionDateBetween(
                        event.getUserId(),
                        LocalDate.of(year, month, 1),
                        LocalDate.of(year, month,
                                java.time.YearMonth.of(year, month).lengthOfMonth())
                );

        BigDecimal totalSpent = txns.stream()
                .filter(t -> t.getCategory().getId().equals(event.getCategoryId())
                        && t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limit = budget.getMonthlyLimit();
        double pct = totalSpent.divide(limit, 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null) return;

        // 80% warning
        if (pct >= 80 && pct < 100) {
            saveNotification(user,
                    String.format("⚠️ You've used %.0f%% of your %s budget (₹%.0f / ₹%.0f)",
                            pct, event.getCategoryName(),
                            totalSpent, limit),
                    "BUDGET_WARNING");
            log.warn("Budget WARNING for user {} category {} — {:.0f}%",
                    event.getUserId(), event.getCategoryName(), pct);
        }

        // 100% exceeded
        if (pct >= 100) {
            saveNotification(user,
                    String.format("🚨 Budget EXCEEDED for %s! Spent ₹%.0f of ₹%.0f limit",
                            event.getCategoryName(), totalSpent, limit),
                    "BUDGET_EXCEEDED");
            log.error("Budget EXCEEDED for user {} category {}",
                    event.getUserId(), event.getCategoryName());
        }
    }

    private void saveNotification(User user, String message, String type) {
        notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .message(message)
                        .type(type)
                        .isRead(false)
                        .build()
        );
    }
}