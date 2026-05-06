package com.expensia.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private Long categoryId;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;
    private Boolean isRecurring;

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }
}