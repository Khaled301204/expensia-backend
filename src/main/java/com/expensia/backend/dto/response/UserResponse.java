package com.expensia.backend.dto.response;

import com.expensia.backend.model.enums.RiskPreference;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private RiskPreference riskPreference;
    private LocalDateTime createdAt;
}