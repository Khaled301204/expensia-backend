package com.expensia.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeResponse {

    private Long incomeId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime date;
    private String source;
    private String frequency;
    private Boolean isRecurring;
    private LocalDateTime nextOccurrence;
    private Boolean recurringActive;

    public IncomeResponse(Long incomeId, Long userId, BigDecimal amount, LocalDateTime date,
                          String source, String frequency, Boolean isRecurring, LocalDateTime nextOccurrence, Boolean recurringActive) {
        this.incomeId = incomeId;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.source = source;
        this.frequency = frequency;
        this.isRecurring = isRecurring;
        this.nextOccurrence = nextOccurrence;
        this.recurringActive = recurringActive;
    }

    public Long getIncomeId() { return incomeId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getSource() { return source; }
    public String getFrequency() { return frequency; }
    public Boolean getIsRecurring() { return isRecurring; }
    public LocalDateTime getNextOccurrence(){ return nextOccurrence; }
    public Boolean getRecurringActive() { return recurringActive; }
}