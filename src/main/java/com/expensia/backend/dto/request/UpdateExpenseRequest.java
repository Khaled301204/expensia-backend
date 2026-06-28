package com.expensia.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateExpenseRequest {

    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;
    private Boolean isRecurring;
    private String frequency;
    private Boolean recurringActive;

    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getFrequency() { return frequency; }
    public Boolean getIsRecurring() { return isRecurring; }
    public Boolean getRecurringActive() { return recurringActive; }
}