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

    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public String getPaymentMethod() { return paymentMethod; }
    public Boolean getIsRecurring() { return isRecurring; }
}