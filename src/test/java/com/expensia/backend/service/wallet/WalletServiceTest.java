package com.expensia.backend.service.wallet;

import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet walletWithBalance(Long userId, BigDecimal balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrentSavings(balance);
        return wallet;
    }

    @Test
    void increaseSavings_addsAmountToExistingWallet() {
        Wallet existing = walletWithBalance(1L, new BigDecimal("100.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        walletService.increaseSavings(1L, new BigDecimal("50.00"));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getCurrentSavings());
    }

    @Test
    void decreaseSavings_subtractsAmountFromExistingWallet() {
        Wallet existing = walletWithBalance(1L, new BigDecimal("200.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        walletService.decreaseSavings(1L, new BigDecimal("80.00"));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertEquals(new BigDecimal("120.00"), captor.getValue().getCurrentSavings());
    }

    @Test
    void increaseSavings_createsWalletIfNotExists() {
        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        walletService.increaseSavings(99L, new BigDecimal("30.00"));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertEquals(new BigDecimal("30.00"), captor.getValue().getCurrentSavings());
    }

    @Test
    void decreaseSavings_allowsNegativeBalance() {
        Wallet existing = walletWithBalance(1L, new BigDecimal("10.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        walletService.decreaseSavings(1L, new BigDecimal("50.00"));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertEquals(new BigDecimal("-40.00"), captor.getValue().getCurrentSavings());
    }
}
