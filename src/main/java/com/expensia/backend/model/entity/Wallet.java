package com.expensia.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal currentSavings;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {

        if (currentSavings == null) {
            currentSavings = BigDecimal.ZERO;
        }

        updatedAt = LocalDateTime.now();
    }
}