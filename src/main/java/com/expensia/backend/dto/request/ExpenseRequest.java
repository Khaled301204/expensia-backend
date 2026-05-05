package com.expensia.backend.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequest {
    private BigDecimal amount;
    private Long categoryId;
    private LocalDateTime date;
    private String description;
    private String merchant;
    private String paymentMethod;
}
