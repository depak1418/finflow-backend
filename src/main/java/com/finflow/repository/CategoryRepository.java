// CategoryRepository.java
package com.finflow.repository;

import com.finflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(Category.CategoryType type);
}