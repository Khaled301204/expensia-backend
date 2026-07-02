package com.expensia.backend.service.analysis;

import com.expensia.backend.dto.response.DashboardResponse;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.repository.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private IncomeRepository incomeRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private GoalRepository goalRepository;

    @InjectMocks
    private DashboardService dashboardService;

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

    @Test
    void getSummary_returnsCorrectTotals() {
        when(incomeRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("5000"));
        when(expenseRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("2000"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(walletWith(new BigDecimal("3000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(List.of(goal(), goal()));
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(new Budget()));

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(new BigDecimal("5000"), response.getTotalIncome());
        assertEquals(new BigDecimal("2000"), response.getTotalExpenses());
        assertEquals(new BigDecimal("3000"), response.getCurrentBalance());
        assertEquals(new BigDecimal("3000"), response.getCurrentSavings());
        assertEquals(2, response.getActiveGoals());
        assertEquals(1, response.getTotalBudgets());
    }

    @Test
    void getSummary_nullIncome_treatedAsZero() {
        when(incomeRepository.sumByUserIdAndDateBetween(eq(1L), any(), any())).thenReturn(null);
        when(expenseRepository.sumByUserIdAndDateBetween(eq(1L), any(), any())).thenReturn(null);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findByUserId(1L)).thenReturn(List.of());
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(BigDecimal.ZERO, response.getTotalIncome());
        assertEquals(BigDecimal.ZERO, response.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance());
        assertEquals(BigDecimal.ZERO, response.getCurrentSavings());
    }

    @Test
    void getSummary_noWallet_savingsIsZero() {
        when(incomeRepository.sumByUserIdAndDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumByUserIdAndDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findByUserId(1L)).thenReturn(List.of());
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(BigDecimal.ZERO, response.getCurrentSavings());
    }

    private Wallet walletWith(BigDecimal savings) {
        Wallet w = new Wallet();
        w.setUserId(1L);
        w.setCurrentSavings(savings);
        return w;
    }

    private SavingGoal goal() {
        SavingGoal g = new SavingGoal();
        g.setUserId(1L);
        g.setName("Goal");
        g.setTargetAmount(new BigDecimal("1000"));
        g.setCurrentAmount(BigDecimal.ZERO);
        g.setDeadline(LocalDate.now().plusMonths(6));
        g.setStatus(GoalStatus.ACTIVE);
        return g;
    }
}
