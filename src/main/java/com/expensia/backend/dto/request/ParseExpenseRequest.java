package com.expensia.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ParseExpenseRequest {

    @NotBlank(message = "Text is required")
    private String text;

    public String getText() {
        return text;
    }
}