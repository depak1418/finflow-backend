// TransactionRepository.java
package com.finflow.repository;

import com.finflow.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId, LocalDate start, LocalDate end);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type " +
            "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    BigDecimal sumByUserAndTypeAndMonthYear(
            @Param("userId") Long userId,
            @Param("type") Transaction.TransactionType type,
            @Param("month") int month,
            @Param("year") int year);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year " +
            "GROUP BY t.category.name")
    List<Object[]> getExpenseByCategory(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);
}