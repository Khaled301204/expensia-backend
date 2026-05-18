package com.expensia.backend.dto.response;

import java.util.Map;

public class VoiceExpenseResponse {

    private boolean success;
    private Double amount;
    private String merchant;
    private String description;
    private String date;
    private String category;
    private Double confidence;
    private Map<String, Object> speechMetadata;

    public boolean isSuccess() {
        return success;
    }

    public Double getAmount() {
        return amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Map<String, Object> getSpeechMetadata() {
        return speechMetadata;
    }
}