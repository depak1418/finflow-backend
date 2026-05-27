package com.finflow.service;

import com.finflow.dto.BudgetResponse;
import com.finflow.dto.DashboardResponse;
import com.finflow.entity.Transaction;
import com.finflow.entity.User;
import com.finflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ✅ Redis caches this for 10 min — key = "dashboard::userId_month_year"
    @Cacheable(value = "dashboard",
            key = "#userId + '_' + #month + '_' + #year")
    public DashboardResponse getDashboard(Long userId, int month, int year) {
        log.info("Cache MISS — querying DB for dashboard userId={} {}/{}",
                userId, month, year);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = LocalDate.of(year, month,
                YearMonth.of(year, month).lengthOfMonth());

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(userId, start, end);

        // Total income
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total expense
        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Expense grouped by category
        Map<String, BigDecimal> expenseByCategory = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO,
                                Transaction::getAmount, BigDecimal::add)
                ));

        // Budget health
        List<BudgetResponse> budgets = budgetRepository
                .findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(b -> {
                    BigDecimal spent = expenseByCategory
                            .getOrDefault(b.getCategory().getName(), BigDecimal.ZERO);
                    return BudgetResponse.from(b, spent);
                })
                .toList();

        // Unread notifications count
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .month(month)
                .year(year)
                .expenseByCategory(expenseByCategory)
                .budgets(budgets)
                .unreadNotifications(unread)
                .build();
    }

    // ✅ Evict cache when transactions change — called from TransactionService
    @CacheEvict(value = "dashboard", allEntries = true)
    public void evictDashboardCache() {
        log.info("Dashboard cache evicted");
    }
}