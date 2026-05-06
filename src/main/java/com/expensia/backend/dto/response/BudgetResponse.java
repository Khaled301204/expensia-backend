package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetResponse {

    private Long budgetId;
    private Long categoryId;
    private BigDecimal limitAmount;
    private LocalDate startDate;
    private LocalDate endDate;

    public BudgetResponse(
            Long budgetId,
            Long categoryId,
            BigDecimal limitAmount,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}