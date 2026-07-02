package com.expensia.backend.service.goal;

import com.expensia.backend.dto.request.GoalRequest;
import com.expensia.backend.dto.response.GoalResponse;
import com.expensia.backend.dto.response.WalletResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.repository.GoalRepository;
import com.expensia.backend.service.wallet.WalletService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private GoalService goalService;

    private User currentUser;

    @BeforeEach
    void setUpSecurityContext() {
        currentUser = User.builder().userId(1L).email("user@example.com").name("User").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private GoalRequest buildRequest(String name, BigDecimal target, BigDecimal current) {
        GoalRequest req = new GoalRequest();
        setField(req, "name", name);
        setField(req, "targetAmount", target);
        setField(req, "currentAmount", current);
        setField(req, "deadline", LocalDate.now().plusMonths(6));
        return req;
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

    private SavingGoal savedGoal(Long id, BigDecimal target, BigDecimal current, GoalStatus status) {
        SavingGoal goal = new SavingGoal();
        goal.setGoalId(id);
        goal.setUserId(1L);
        goal.setName("Test Goal");
        goal.setTargetAmount(target);
        goal.setCurrentAmount(current);
        goal.setDeadline(LocalDate.now().plusMonths(6));
        goal.setStatus(status);
        goal.setCreatedAt(LocalDateTime.now());
        return goal;
    }

    @Test
    void createGoal_withoutInitialAmount_setsZeroAndDoesNotDeductWallet() {
        GoalRequest request = buildRequest("Vacation", new BigDecimal("5000"), null);
        SavingGoal saved = savedGoal(1L, new BigDecimal("5000"), BigDecimal.ZERO, GoalStatus.ACTIVE);
        when(goalRepository.save(any())).thenReturn(saved);

        GoalResponse response = goalService.createGoal(request);

        assertEquals(GoalStatus.ACTIVE, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getCurrentAmount());
        verify(walletService, never()).decreaseSavings(any(), any());
    }

    @Test
    void createGoal_withInitialAmount_deductsWallet() {
        GoalRequest request = buildRequest("Car", new BigDecimal("10000"), new BigDecimal("500"));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("1000"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);
        SavingGoal saved = savedGoal(1L, new BigDecimal("10000"), new BigDecimal("500"), GoalStatus.ACTIVE);
        when(goalRepository.save(any())).thenReturn(saved);

        goalService.createGoal(request);

        verify(walletService).decreaseSavings(1L, new BigDecimal("500"));
    }

    @Test
    void createGoal_initialAmountExceedsWalletBalance_throws() {
        GoalRequest request = buildRequest("Car", new BigDecimal("10000"), new BigDecimal("500"));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("100"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);

        assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(request));
        verify(goalRepository, never()).save(any());
    }

    @Test
    void createGoal_initialAmountEqualsTarget_setsCompleted() {
        GoalRequest request = buildRequest("Emergency Fund", new BigDecimal("1000"), new BigDecimal("1000"));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("2000"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);
        SavingGoal saved = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("1000"), GoalStatus.COMPLETED);
        when(goalRepository.save(any())).thenReturn(saved);

        GoalResponse response = goalService.createGoal(request);

        assertEquals(GoalStatus.COMPLETED, response.getStatus());
    }

    @Test
    void createGoal_negativeInitialAmount_throws() {
        GoalRequest request = buildRequest("Goal", new BigDecimal("1000"), new BigDecimal("-1"));

        assertThrows(IllegalArgumentException.class, () -> goalService.createGoal(request));
    }

    @Test
    void addSavings_happyPath_increasesCurrentAmount() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("200"), GoalStatus.ACTIVE);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("500"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);
        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.addSavings(1L, new BigDecimal("100"));

        assertEquals(new BigDecimal("300"), response.getCurrentAmount());
        assertEquals(GoalStatus.ACTIVE, response.getStatus());
        verify(walletService).decreaseSavings(1L, new BigDecimal("100"));
    }

    @Test
    void addSavings_reachesTarget_setsCompleted() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("900"), GoalStatus.ACTIVE);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("500"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);
        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.addSavings(1L, new BigDecimal("200"));

        assertEquals(new BigDecimal("1000"), response.getCurrentAmount());
        assertEquals(GoalStatus.COMPLETED, response.getStatus());
    }

    @Test
    void addSavings_insufficientWalletBalance_throws() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("200"), GoalStatus.ACTIVE);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        WalletResponse wallet = new WalletResponse(1L, new BigDecimal("50"), LocalDateTime.now());
        when(walletService.getWallet()).thenReturn(wallet);

        assertThrows(IllegalArgumentException.class, () -> goalService.addSavings(1L, new BigDecimal("100")));
        verify(walletService, never()).decreaseSavings(any(), any());
    }

    @Test
    void addSavings_toCompletedGoal_throws() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("1000"), GoalStatus.COMPLETED);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(IllegalArgumentException.class, () -> goalService.addSavings(1L, new BigDecimal("100")));
    }

    @Test
    void addSavings_goalNotFound_throws() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.addSavings(99L, new BigDecimal("100")));
    }

    @Test
    void addSavings_zeroAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> goalService.addSavings(1L, BigDecimal.ZERO));
    }

    @Test
    void withdrawSavings_happyPath_decreasesCurrentAmount() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("500"), GoalStatus.ACTIVE);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.withdrawSavings(1L, new BigDecimal("200"));

        assertEquals(new BigDecimal("300"), response.getCurrentAmount());
        assertEquals(GoalStatus.ACTIVE, response.getStatus());
        verify(walletService).increaseSavings(1L, new BigDecimal("200"));
    }

    @Test
    void withdrawSavings_fromCompletedGoal_revertsToActive() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("1000"), GoalStatus.COMPLETED);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = goalService.withdrawSavings(1L, new BigDecimal("100"));

        assertEquals(new BigDecimal("900"), response.getCurrentAmount());
        assertEquals(GoalStatus.ACTIVE, response.getStatus());
    }

    @Test
    void withdrawSavings_moreThanCurrentAmount_throws() {
        SavingGoal goal = savedGoal(1L, new BigDecimal("1000"), new BigDecimal("100"), GoalStatus.ACTIVE);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(IllegalArgumentException.class, () -> goalService.withdrawSavings(1L, new BigDecimal("200")));
        verify(walletService, never()).increaseSavings(any(), any());
    }
}
