package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetResponse {

    private Long budgetId;
    private Long userId;
    private Long categoryId;
    private BigDecimal limitAmount;
    private Double alertThreshold;
    private BigDecimal spentAmount;
    private Boolean isOverBudget;
    private LocalDate startDate;
    private LocalDate endDate;

    public BudgetResponse(
            Long budgetId,
            Long userId,
            Long categoryId,
            BigDecimal limitAmount,
            BigDecimal spentAmount,
            Double alertThreshold,
            Boolean isOverBudget,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.alertThreshold = alertThreshold;
        this.isOverBudget = isOverBudget;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getBudgetId() {
        return budgetId;
    }
    public Long getUserId() {
        return userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getLimitAmount() { return limitAmount; }

    public Double getAlertThreshold() { return alertThreshold; }

    public BigDecimal getSpentAmount() { return spentAmount; }

    public Boolean getIsOverBudget() {
        return isOverBudget;
    }

    public LocalDate getStartDate() { return startDate; }

    public LocalDate getEndDate() {
        return endDate;
    }
}