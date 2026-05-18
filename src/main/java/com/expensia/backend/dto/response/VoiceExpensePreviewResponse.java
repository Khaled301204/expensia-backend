package com.expensia.backend.dto.response;

import java.util.Map;

public class VoiceExpensePreviewResponse {

    private Double amount;
    private String merchant;
    private String description;
    private String date;
    private Long categoryId;
    private String categoryName;
    private Double categoryConfidence;
    private Map<String, Object> speechMetadata;

    public VoiceExpensePreviewResponse(
            Double amount,
            String merchant,
            String description,
            String date,
            Long categoryId,
            String categoryName,
            Double categoryConfidence,
            Map<String, Object> speechMetadata
    ) {
        this.amount = amount;
        this.merchant = merchant;
        this.description = description;
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryConfidence = categoryConfidence;
        this.speechMetadata = speechMetadata;
    }

    public Double getAmount() { return amount; }
    public String getMerchant() { return merchant; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Double getCategoryConfidence() { return categoryConfidence; }
    public Map<String, Object> getSpeechMetadata() { return speechMetadata; }
}