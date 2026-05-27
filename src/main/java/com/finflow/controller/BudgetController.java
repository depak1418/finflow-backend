package com.finflow.controller;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create or update a budget for a category")
    public ResponseEntity<BudgetResponse> createOrUpdate(
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(request));
    }

    @GetMapping
    @Operation(summary = "Get budgets for a month/year")
    public ResponseEntity<List<BudgetResponse>> getByMonthYear(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgetsByMonthYear(month, year));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}