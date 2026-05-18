package com.expensia.backend.service.wallet;

import com.expensia.backend.dto.request.WalletRequest;
import com.expensia.backend.dto.response.WalletResponse;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.repository.WalletRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletResponse getWallet() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Wallet wallet = walletRepository.findByUserId(
                currentUser.getUserId()
        ).orElseGet(() -> {

            Wallet newWallet = new Wallet();

            newWallet.setUserId(currentUser.getUserId());
            newWallet.setCurrentSavings(BigDecimal.ZERO);

            return walletRepository.save(newWallet);
        });

        return mapToResponse(wallet);
    }

    public WalletResponse updateWallet(WalletRequest request) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Wallet wallet = walletRepository.findByUserId(
                currentUser.getUserId()
        ).orElseGet(() -> {

            Wallet newWallet = new Wallet();

            newWallet.setUserId(currentUser.getUserId());
            newWallet.setCurrentSavings(BigDecimal.ZERO);

            return newWallet;
        });

        wallet.setCurrentSavings(request.getCurrentSavings());

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    public void increaseSavings(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setCurrentSavings(BigDecimal.ZERO);
                    return newWallet;
                });

        wallet.setCurrentSavings(wallet.getCurrentSavings().add(amount));

        walletRepository.save(wallet);
    }

    public void decreaseSavings(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(userId);
                    newWallet.setCurrentSavings(BigDecimal.ZERO);
                    return newWallet;
                });

        wallet.setCurrentSavings(wallet.getCurrentSavings().subtract(amount));

        walletRepository.save(wallet);
    }

    private WalletResponse mapToResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getWalletId(),
                wallet.getCurrentSavings(),
                wallet.getUpdatedAt()
        );
    }
}