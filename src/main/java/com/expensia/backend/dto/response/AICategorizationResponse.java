package com.expensia.backend.dto.response;

public class AICategorizationResponse {

    private boolean success;
    private String category;
    private Double confidence;

    public boolean isSuccess() {
        return success;
    }

    public String getCategory() {
        return category;
    }

    public Double getConfidence() {
        return confidence;
    }
}