package com.expensia.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Long categoryId;
    private String categoryName;
    private Double categoryConfidence;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private String description;
    private String merchant;
    private String paymentMethod;

    private Boolean isRecurring;
    private String frequency;
    private LocalDateTime nextOccurrence;
    private Boolean recurringActive;
    private Boolean createdByVoice;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (isRecurring == null) {
            isRecurring = false;
        }

        if (createdByVoice == null) {
            createdByVoice = false;
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