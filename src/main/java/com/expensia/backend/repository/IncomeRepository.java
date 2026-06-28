package com.expensia.backend.repository;

import com.expensia.backend.model.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId " +
            "AND i.date >= :start AND i.date < :end")
    BigDecimal sumByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Income> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Income> findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(
            LocalDateTime now
    );
}
