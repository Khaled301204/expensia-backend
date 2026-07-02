package com.expensia.backend.service.wallet;

import com.expensia.backend.dto.request.WalletRequest;
import com.expensia.backend.dto.response.WalletResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceExtendedTest {

    @Mock private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        User user = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Wallet walletWith(BigDecimal savings) {
        Wallet w = new Wallet();
        w.setWalletId(1L);
        w.setUserId(1L);
        w.setCurrentSavings(savings);
        return w;
    }

    @Test
    void getWallet_existingWallet_returnsIt() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(walletWith(new BigDecimal("500"))));

        WalletResponse response = walletService.getWallet();

        assertEquals(new BigDecimal("500"), response.getCurrentSavings());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void getWallet_noWallet_createsAndReturnsNew() {
        Wallet saved = walletWith(BigDecimal.ZERO);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenReturn(saved);

        WalletResponse response = walletService.getWallet();

        assertEquals(BigDecimal.ZERO, response.getCurrentSavings());
        verify(walletRepository).save(any());
    }

    @Test
    void updateWallet_setsNewBalance() {
        Wallet existing = walletWith(new BigDecimal("100"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WalletRequest req = new WalletRequest();
        setField(req, "currentSavings", new BigDecimal("999"));

        WalletResponse response = walletService.updateWallet(req);

        assertEquals(new BigDecimal("999"), response.getCurrentSavings());
    }

    @Test
    void updateWallet_noExistingWallet_createsAndSets() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WalletRequest req = new WalletRequest();
        setField(req, "currentSavings", new BigDecimal("200"));

        WalletResponse response = walletService.updateWallet(req);

        assertEquals(new BigDecimal("200"), response.getCurrentSavings());
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
