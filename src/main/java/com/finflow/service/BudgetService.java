package com.finflow.service;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.entity.Budget;
import com.finflow.entity.Category;
import com.finflow.entity.Transaction;
import com.finflow.entity.User;
import com.finflow.repository.BudgetRepository;
import com.finflow.repository.CategoryRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public BudgetResponse createOrUpdateBudget(BudgetRequest request) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Upsert — update if exists, create if not
        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(), category.getId(),
                        request.getMonth(), request.getYear())
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());

        budget.setMonthlyLimit(request.getMonthlyLimit());
        Budget saved = budgetRepository.save(budget);

        BigDecimal spent = getSpentAmount(
                user.getId(), category.getId(),
                request.getMonth(), request.getYear());

        return BudgetResponse.from(saved, spent);
    }

    public List<BudgetResponse> getBudgetsByMonthYear(int month, int year) {
        User user = getCurrentUser();
        return budgetRepository
                .findByUserIdAndMonthAndYear(user.getId(), month, year)
                .stream()
                .map(b -> {
                    BigDecimal spent = getSpentAmount(
                            user.getId(), b.getCategory().getId(), month, year);
                    return BudgetResponse.from(b, spent);
                })
                .toList();
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        budgetRepository.delete(budget);
    }

    private BigDecimal getSpentAmount(
            Long userId, Long categoryId, int month, int year) {
        List<Transaction> txns = transactionRepository
                .findByUserIdAndTransactionDateBetween(
                        userId,
                        java.time.LocalDate.of(year, month, 1),
                        java.time.LocalDate.of(year, month,
                                java.time.YearMonth.of(year, month).lengthOfMonth())
                );
        return txns.stream()
                .filter(t -> t.getCategory().getId().equals(categoryId)
                        && t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}