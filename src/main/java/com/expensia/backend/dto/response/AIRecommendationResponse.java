package com.expensia.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class AIRecommendationResponse {

    private boolean success;

    @JsonProperty("spending_insights")
    private List<Map<String, Object>> spendingInsights;

    @JsonProperty("saving_recommendations")
    private Map<String, Object> savingRecommendations;

    @JsonProperty("investment_suggestions")
    private List<Map<String, Object>> investmentSuggestions;

    @JsonProperty("goal_plans")
    private List<Map<String, Object>> goalPlans;

    @JsonProperty("overall_score")
    private Double overallScore;

    public boolean isSuccess() {
        return success;
    }

    public List<Map<String, Object>> getSpendingInsights() {
        return spendingInsights;
    }

    public Map<String, Object> getSavingRecommendations() {
        return savingRecommendations;
    }

    public List<Map<String, Object>> getInvestmentSuggestions() {
        return investmentSuggestions;
    }

    public List<Map<String, Object>> getGoalPlans() {
        return goalPlans;
    }

    public Double getOverallScore() {
        return overallScore;
    }
}