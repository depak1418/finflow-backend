package com.finflow.controller;

import com.finflow.dto.CategoryResponse;
import com.finflow.entity.Category;
import com.finflow.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type (INCOME or EXPENSE)")
    public ResponseEntity<List<CategoryResponse>> getByType(
            @PathVariable Category.CategoryType type) {
        return ResponseEntity.ok(categoryService.getCategoriesByType(type));
    }
}