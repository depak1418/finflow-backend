package com.finflow.entity;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // FOOD, TRANSPORT, SALARY, etc.

    private String icon;  // emoji or icon name for UI

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    public enum CategoryType {
        INCOME, EXPENSE
    }
}