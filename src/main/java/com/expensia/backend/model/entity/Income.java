package com.expensia.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes")
@Data
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incomeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private String source;
    private String frequency;
    private Boolean isRecurring;
    private LocalDateTime nextOccurrence;
    private Boolean recurringActive;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }

        if (isRecurring == null) {
            isRecurring = false;
        }

        if (recurringActive == null) {
            recurringActive = Boolean.TRUE.equals(isRecurring);
        }

        if (Boolean.TRUE.equals(isRecurring) && nextOccurrence == null) {
            nextOccurrence = calculateNextOccurrence(date, frequency);
        }
    }

    private LocalDateTime calculateNextOccurrence(LocalDateTime date, String frequency) {
        if (date == null || frequency == null) {
            return null;
        }

        return switch (frequency.toUpperCase()) {
            case "DAILY" -> date.plusDays(1);
            case "WEEKLY" -> date.plusWeeks(1);
            case "MONTHLY" -> date.plusMonths(1);
            case "YEARLY" -> date.plusYears(1);
            default -> null;
        };
    }
}