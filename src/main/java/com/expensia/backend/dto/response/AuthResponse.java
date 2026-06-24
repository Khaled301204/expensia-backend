package com.expensia.backend.dto.response;

import com.expensia.backend.model.enums.RiskPreference;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String name;
    private RiskPreference riskPreference;
}
