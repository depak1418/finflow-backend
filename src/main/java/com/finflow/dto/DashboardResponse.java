package com.finflow.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;         // income - expense
    private int month;
    private int year;

    // For pie/bar chart — category name → amount spent
    private Map<String, BigDecimal> expenseByCategory;

    // Budget health — how much used per category
    private List<BudgetResponse> budgets;

    // Unread notification count
    private long unreadNotifications;
}