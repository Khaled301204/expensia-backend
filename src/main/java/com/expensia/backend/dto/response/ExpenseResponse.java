package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long expenseId;
    private Long userId;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private Double categoryConfidence;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;
    private Boolean isRecurring;
    private String frequency;
    private LocalDateTime nextOccurrence;
    private Boolean recurringActive;
    private Boolean createdByVoice;
    private LocalDateTime createdAt;

    public ExpenseResponse(Long expenseId, Long userId, BigDecimal amount, Long categoryId,String categoryName,Double categoryConfidence,
                           LocalDateTime date, String description, String merchant,
                           String paymentMethod, Boolean isRecurring, String frequency, LocalDateTime nextOccurrence, Boolean recurringActive, Boolean createdByVoice, LocalDateTime createdAt) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryConfidence = categoryConfidence;
        this.date = date;
        this.description = description;
        this.merchant = merchant;
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring;
        this.frequency = frequency;
        this.nextOccurrence = nextOccurrence;
        this.recurringActive = recurringActive;
        this.createdByVoice = createdByVoice;
        this.createdAt = createdAt;
    }

    public Long getExpenseId() { return expenseId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Double getCategoryConfidence() { return categoryConfidence; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public String getPaymentMethod() { return paymentMethod; }
    public Boolean getIsRecurring() { return isRecurring; }
    public String getFrequency() { return frequency; }
    public LocalDateTime getNextOccurrence() { return nextOccurrence; }
    public Boolean getRecurringActive() { return recurringActive; }
    public Boolean getCreatedByVoice() { return createdByVoice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}