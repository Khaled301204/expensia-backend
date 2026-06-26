package com.expensia.backend.repository;

import com.expensia.backend.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

    Optional<Budget> findFirstByUserIdAndCategoryId(Long userId, Long categoryId);
}