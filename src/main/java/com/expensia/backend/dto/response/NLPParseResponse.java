package com.expensia.backend.dto.response;

import java.util.Map;

public class NLPParseResponse {

    private boolean success;
    private ParsedExpense parsed;
    private String original_text;

    public boolean isSuccess() {
        return success;
    }

    public ParsedExpense getParsed() {
        return parsed;
    }

    public String getOriginal_text() {
        return original_text;
    }

    public static class ParsedExpense {

        private Double amount;
        private String merchant;
        private String category;
        private String description;
        private String date;

        private Map<String, Double> confidence;

        public Double getAmount() {
            return amount;
        }

        public String getMerchant() {
            return merchant;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public String getDate() {
            return date;
        }

        public Map<String, Double> getConfidence() {
            return confidence;
        }
    }
}