package com.expensia.backend.controller;

import com.expensia.backend.dto.request.WalletRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.WalletResponse;
import com.expensia.backend.service.wallet.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ApiResponse<WalletResponse> getWallet() {
        return ApiResponse.success(
                "Wallet retrieved successfully",
                walletService.getWallet()
        );
    }

    @PutMapping
    public ApiResponse<WalletResponse> updateWallet(@RequestBody WalletRequest request) {
        return ApiResponse.success(
                "Wallet updated successfully",
                walletService.updateWallet(request)
        );
    }
}