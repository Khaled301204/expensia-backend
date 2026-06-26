package com.expensia.backend.dto.request;

import com.expensia.backend.model.enums.RiskPreference;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String name;
    private String phone;
    private RiskPreference riskPreference;
}