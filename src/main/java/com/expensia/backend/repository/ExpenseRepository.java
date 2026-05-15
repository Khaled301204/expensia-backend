package com.expensia.backend.repository;


import com.expensia.backend.model.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByDateDesc(Long userId);
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId " +
            "AND e.date >= :start AND e.date < :end")
    BigDecimal sumByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
    AND e.categoryId = :categoryId
    AND e.date BETWEEN :startDate AND :endDate
""")
    BigDecimal sumByUserIdAndCategoryIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
