package com.expensia.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal limitAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
    private Double alertThreshold;

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
    public Double getAlertThreshold() { return alertThreshold; }
}