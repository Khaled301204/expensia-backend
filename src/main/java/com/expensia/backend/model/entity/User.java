package com.expensia.backend.model.entity;

import com.expensia.backend.model.enums.RiskPreference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String passwordHash;

    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_preference")
    @Builder.Default
    private RiskPreference riskPreference = RiskPreference.MEDIUM;

    @CreationTimestamp
    private LocalDateTime createdAt;
}