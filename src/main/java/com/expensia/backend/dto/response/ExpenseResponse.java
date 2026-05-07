package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long expenseId;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private Double categoryConfidence;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;
    private Boolean isRecurring;
    private Boolean createdByVoice;

    public ExpenseResponse(Long expenseId, BigDecimal amount, Long categoryId,String categoryName,Double categoryConfidence,
                           LocalDateTime date, String description, String merchant,
                           String paymentMethod, Boolean isRecurring, Boolean createdByVoice) {
        this.expenseId = expenseId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryConfidence = categoryConfidence;
        this.date = date;
        this.description = description;
        this.merchant = merchant;
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring;
        this.createdByVoice = createdByVoice;
    }

    public Long getExpenseId() { return expenseId; }
    public BigDecimal getAmount() { return amount; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getCategoryConfidence() { return categoryConfidence; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }
    public String getMerchant() { return merchant; }
    public String getPaymentMethod() { return paymentMethod; }
    public Boolean getIsRecurring() { return isRecurring; }
    public Boolean getCreatedByVoice() { return createdByVoice; }
}