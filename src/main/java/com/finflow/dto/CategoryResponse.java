package com.finflow.dto;

import com.finflow.entity.Category;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String icon;
    private Category.CategoryType type;

    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .icon(c.getIcon())
                .type(c.getType())
                .build();
    }
}