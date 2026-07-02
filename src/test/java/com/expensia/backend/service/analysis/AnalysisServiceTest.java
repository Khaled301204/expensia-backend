package com.expensia.backend.service.analysis;

import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private IncomeRepository incomeRepository;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void getMonthlyExpenses_returnsRepositoryValue() {
        when(expenseRepository.sumByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1500"));

        BigDecimal result = analysisService.getMonthlyExpenses(1L, 2026, 7);

        assertEquals(new BigDecimal("1500"), result);
    }

    @Test
    void getMonthlyIncome_returnsRepositoryValue() {
        when(incomeRepository.sumByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("5000"));

        BigDecimal result = analysisService.getMonthlyIncome(1L, 2026, 7);

        assertEquals(new BigDecimal("5000"), result);
    }

    @Test
    void getBalance_returnsIncomeMinusExpenses() {
        when(incomeRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("5000"));
        when(expenseRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("2000"));

        BigDecimal balance = analysisService.getBalance(1L, 2026, 7);

        assertEquals(new BigDecimal("3000"), balance);
    }

    @Test
    void getBalance_expensesExceedIncome_returnsNegative() {
        when(incomeRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("1000"));
        when(expenseRepository.sumByUserIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(new BigDecimal("1500"));

        BigDecimal balance = analysisService.getBalance(1L, 2026, 7);

        assertTrue(balance.compareTo(BigDecimal.ZERO) < 0);
    }
}
