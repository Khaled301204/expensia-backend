package com.expensia.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateIncomeRequest {

    private BigDecimal amount;
    private LocalDateTime date;
    private String source;
    private String frequency;
    private Boolean isRecurring;
    private Boolean recurringActive;

    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getSource() { return source; }
    public String getFrequency() { return frequency; }
    public Boolean getIsRecurring() { return isRecurring; }
    public Boolean getRecurringActive() { return recurringActive; }
}