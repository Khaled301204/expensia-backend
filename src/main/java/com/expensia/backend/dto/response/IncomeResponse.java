package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeResponse {

    private Long incomeId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String source;
    private String frequency;
    private Boolean isRecurring;

    public IncomeResponse(Long incomeId, BigDecimal amount, LocalDateTime date,
                          String source, String frequency, Boolean isRecurring) {
        this.incomeId = incomeId;
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.frequency = frequency;
        this.isRecurring = isRecurring;
    }

    public Long getIncomeId() { return incomeId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getSource() { return source; }
    public String getFrequency() { return frequency; }
    public Boolean getIsRecurring() { return isRecurring; }
}