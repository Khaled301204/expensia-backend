package com.expensia.backend.repository;

import com.expensia.backend.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);
}