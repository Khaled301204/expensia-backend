package com.expensia.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConfirmVoiceExpenseRequest {

    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private Double categoryConfidence;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;

    public BigDecimal getAmount() { return amount; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Double getCategoryConfidence() { return categoryConfidence; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public String getPaymentMethod() { return paymentMethod; }
}