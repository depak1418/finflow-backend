package com.finflow.service;

import com.finflow.dto.CategoryResponse;
import com.finflow.entity.Category;
import com.finflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Seeds default categories on first startup if table is empty
    @EventListener(ApplicationReadyEvent.class)
    public void seedCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> defaults = List.of(
                Category.builder().name("Food & Dining").icon("🍔")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Transport").icon("🚗")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Shopping").icon("🛍️")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Entertainment").icon("🎬")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Health").icon("💊")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Utilities").icon("💡")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Rent").icon("🏠")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Education").icon("📚")
                        .type(Category.CategoryType.EXPENSE).build(),
                Category.builder().name("Salary").icon("💰")
                        .type(Category.CategoryType.INCOME).build(),
                Category.builder().name("Freelance").icon("💻")
                        .type(Category.CategoryType.INCOME).build(),
                Category.builder().name("Investment").icon("📈")
                        .type(Category.CategoryType.INCOME).build(),
                Category.builder().name("Other").icon("📦")
                        .type(Category.CategoryType.EXPENSE).build()
        );
        categoryRepository.saveAll(defaults);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(CategoryResponse::from).toList();
    }

    public List<CategoryResponse> getCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findByType(type)
                .stream().map(CategoryResponse::from).toList();
    }
}