package com.expensia.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WalletResponse {

    private Long walletId;
    private BigDecimal currentSavings;
    private LocalDateTime updatedAt;
}