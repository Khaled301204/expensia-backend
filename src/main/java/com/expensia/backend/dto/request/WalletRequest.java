package com.expensia.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRequest {

    private BigDecimal currentSavings;
}