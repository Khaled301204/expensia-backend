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
    private Boolean createdByVoice;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
        if (createdByVoice == null) {
            createdByVoice = false;
        }
    }
}