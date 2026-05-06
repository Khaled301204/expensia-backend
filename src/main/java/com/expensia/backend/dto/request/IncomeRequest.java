package com.expensia.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private LocalDateTime date;
    private String source;
    private String frequency;
    private Boolean isRecurring;

    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getSource() { return source; }
    public String getFrequency() { return frequency; }
    public Boolean getIsRecurring() { return isRecurring; }
}