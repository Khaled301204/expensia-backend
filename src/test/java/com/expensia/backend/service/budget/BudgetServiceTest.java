package com.expensia.backend.service.budget;

import com.expensia.backend.dto.request.BudgetRequest;
import com.expensia.backend.dto.response.BudgetResponse;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.ExpenseRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    @BeforeEach
    void setUpSecurityContext() {
        User user = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private BudgetRequest buildRequest(Long categoryId, BigDecimal limit, Double threshold) {
        BudgetRequest req = new BudgetRequest();
        setField(req, "categoryId", categoryId);
        setField(req, "limitAmount", limit);
        setField(req, "startDate", LocalDate.of(2026, 7, 1));
        setField(req, "endDate", LocalDate.of(2026, 7, 31));
        setField(req, "alertThreshold", threshold);
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

    private Budget existingBudget(Long id, Long categoryId, BigDecimal limit) {
        Budget b = new Budget();
        b.setBudgetId(id);
        b.setUserId(1L);
        b.setCategoryId(categoryId);
        b.setLimitAmount(limit);
        b.setStartDate(LocalDate.of(2026, 6, 1));
        b.setEndDate(LocalDate.of(2026, 6, 30));
        b.setAlertThreshold(0.8);
        return b;
    }

    @Test
    void createBudget_newCategory_createsFreshBudget() {
        BudgetRequest request = buildRequest(5L, new BigDecimal("500"), 0.8);
        when(budgetRepository.findFirstByUserIdAndCategoryId(1L, 5L)).thenReturn(Optional.empty());
        when(budgetRepository.save(any())).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setBudgetId(10L);
            return b;
        });
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.createBudget(request);

        assertEquals(5L, response.getCategoryId());
        assertEquals(new BigDecimal("500"), response.getLimitAmount());
        assertFalse(response.getIsOverBudget());
    }

    @Test
    void createBudget_existingCategory_updatesExistingBudget() {
        BudgetRequest request = buildRequest(3L, new BigDecimal("1000"), 0.9);
        Budget existing = existingBudget(7L, 3L, new BigDecimal("500"));
        when(budgetRepository.findFirstByUserIdAndCategoryId(1L, 3L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.createBudget(request);

        assertEquals(7L, response.getBudgetId());
        assertEquals(new BigDecimal("1000"), response.getLimitAmount());
        assertEquals(0.9, response.getAlertThreshold());
    }

    @Test
    void createBudget_defaultThreshold_usesPointEight() {
        BudgetRequest request = buildRequest(2L, new BigDecimal("200"), null);
        when(budgetRepository.findFirstByUserIdAndCategoryId(1L, 2L)).thenReturn(Optional.empty());
        when(budgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.createBudget(request);

        assertEquals(0.8, response.getAlertThreshold());
    }

    @Test
    void getMyBudgets_returnsSpentAmountFromExpenses() {
        Budget budget = existingBudget(1L, 3L, new BigDecimal("500"));
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(
                eq(1L), eq(3L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("300"));

        List<BudgetResponse> budgets = budgetService.getMyBudgets();

        assertEquals(1, budgets.size());
        assertEquals(new BigDecimal("300"), budgets.get(0).getSpentAmount());
        assertFalse(budgets.get(0).getIsOverBudget());
    }

    @Test
    void getMyBudgets_spentExceedsLimit_marksOverBudget() {
        Budget budget = existingBudget(1L, 3L, new BigDecimal("500"));
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("600"));

        List<BudgetResponse> budgets = budgetService.getMyBudgets();

        assertTrue(budgets.get(0).getIsOverBudget());
    }

    @Test
    void getMyBudgets_nullSpentAmount_treatedAsZero() {
        Budget budget = existingBudget(1L, 3L, new BigDecimal("500"));
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(null);

        List<BudgetResponse> budgets = budgetService.getMyBudgets();

        assertEquals(BigDecimal.ZERO, budgets.get(0).getSpentAmount());
    }
}
