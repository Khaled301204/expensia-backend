package com.expensia.backend.dto.response;

import java.util.Map;

public class AIInsightsResponse {

    private boolean success;
    private Map<String, Object> patterns;
    private Map<String, Object> forecast;
    private Map<String, Object> recommendations;

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getPatterns() {
        return patterns;
    }

    public Map<String, Object> getForecast() {
        return forecast;
    }

    public Map<String, Object> getRecommendations() {
        return recommendations;
    }
}