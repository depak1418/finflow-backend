package com.finflow.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "1.0", message = "Limit must be greater than 0")
    private BigDecimal monthlyLimit;

    @Min(1) @Max(12)
    private int month;

    @Min(2024)
    private int year;
}