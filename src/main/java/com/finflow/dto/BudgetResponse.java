package com.finflow.dto;

import com.finflow.entity.Budget;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal monthlyLimit;
    private BigDecimal spent;          // calculated field
    private double percentageUsed;     // spent / limit * 100
    private int month;
    private int year;

    public static BudgetResponse from(Budget b, BigDecimal spent) {
        double pct = b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(b.getMonthlyLimit(), 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return BudgetResponse.builder()
                .id(b.getId())
                .categoryName(b.getCategory().getName())
                .categoryIcon(b.getCategory().getIcon())
                .monthlyLimit(b.getMonthlyLimit())
                .spent(spent)
                .percentageUsed(pct)
                .month(b.getMonth())
                .year(b.getYear())
                .build();
    }
}