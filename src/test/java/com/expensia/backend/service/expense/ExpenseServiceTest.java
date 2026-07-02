package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.service.budget.BudgetAlertService;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private BudgetAlertService budgetAlertService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private ExpenseService expenseService;

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

    private ExpenseRequest buildRequest(BigDecimal amount, String description, String merchant) {
        ExpenseRequest req = new ExpenseRequest();
        setField(req, "amount", amount);
        setField(req, "description", description);
        setField(req, "merchant", merchant);
        setField(req, "date", LocalDateTime.now());
        setField(req, "paymentMethod", "CASH");
        setField(req, "isRecurring", false);
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

    private AICategorizationResponse aiResponse(String category, Double confidence) {
        return new AICategorizationResponse() {
            @Override public boolean isSuccess() { return true; }
            @Override public String getCategory() { return category; }
            @Override public Double getConfidence() { return confidence; }
        };
    }

    @Test
    void createExpense_aiCategorizes_setsAiCategoryOnExpense() {
        ExpenseRequest request = buildRequest(new BigDecimal("50"), "Netflix subscription", "Netflix");
        Category foodCategory = new Category();
        foodCategory.setCategoryId(7L);
        foodCategory.setName("Entertainment");

        when(aiServiceClient.categorizeExpense("Netflix subscription", "Netflix"))
                .thenReturn(aiResponse("Entertainment", 0.95));
        when(categoryRepository.findByNameIgnoreCase("Entertainment"))
                .thenReturn(Optional.of(foodCategory));
        when(expenseRepository.save(any())).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setExpenseId(1L);
            return e;
        });

        ExpenseResponse response = expenseService.createExpense(request);

        assertEquals("Entertainment", response.getCategoryName());
        assertEquals(7L, response.getCategoryId());
        verify(walletService).decreaseSavings(1L, new BigDecimal("50"));
    }

    @Test
    void createExpense_lowAiConfidence_defaultsToOther() {
        ExpenseRequest request = buildRequest(new BigDecimal("20"), "misc", "unknown");

        when(aiServiceClient.categorizeExpense("misc", "unknown"))
                .thenReturn(aiResponse("Food", 0.2));
        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.empty());
        when(expenseRepository.save(any())).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setExpenseId(1L);
            return e;
        });

        ExpenseResponse response = expenseService.createExpense(request);

        assertEquals("Other", response.getCategoryName());
    }

    @Test
    void createExpense_recurringWithoutFrequency_throwsValidationError() {
        ExpenseRequest request = buildRequest(new BigDecimal("50"), "desc", "merchant");
        setField(request, "isRecurring", true);
        setField(request, "frequency", null);

        when(aiServiceClient.categorizeExpense(any(), any()))
                .thenReturn(aiResponse("Other", 0.1));
        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(request));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_recurringWithInvalidFrequency_throws() {
        ExpenseRequest request = buildRequest(new BigDecimal("50"), "desc", "merchant");
        setField(request, "isRecurring", true);
        setField(request, "frequency", "HOURLY");

        when(aiServiceClient.categorizeExpense(any(), any()))
                .thenReturn(aiResponse("Other", 0.1));
        when(categoryRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(request));
    }

    @Test
    void createExpense_recurringMonthly_setsNextOccurrence() {
        LocalDateTime date = LocalDateTime.of(2026, java.time.Month.JULY, 1, 10, 0);
        ExpenseRequest request = buildRequest(new BigDecimal("100"), "Rent", "Landlord");
        setField(request, "date", date);
        setField(request, "isRecurring", true);
        setField(request, "frequency", "MONTHLY");

        when(aiServiceClient.categorizeExpense(any(), any()))
                .thenReturn(aiResponse("Housing", 0.9));
        when(categoryRepository.findByNameIgnoreCase("Housing")).thenReturn(Optional.empty());
        when(expenseRepository.save(any())).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setExpenseId(1L);
            return e;
        });

        ExpenseResponse response = expenseService.createExpense(request);

        assertNotNull(response.getNextOccurrence());
        assertEquals(date.plusMonths(1), response.getNextOccurrence());
        assertTrue(response.getRecurringActive());
    }

    @Test
    void createExpense_deductsWalletAndChecksBudget() {
        ExpenseRequest request = buildRequest(new BigDecimal("75"), "Lunch", "Restaurant");
        when(aiServiceClient.categorizeExpense(any(), any()))
                .thenReturn(aiResponse("Food", 0.85));
        when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.empty());
        when(expenseRepository.save(any())).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setExpenseId(1L);
            return e;
        });

        expenseService.createExpense(request);

        verify(walletService).decreaseSavings(1L, new BigDecimal("75"));
        verify(budgetAlertService).checkBudgetAlert(eq(1L), any(Expense.class));
    }
}
