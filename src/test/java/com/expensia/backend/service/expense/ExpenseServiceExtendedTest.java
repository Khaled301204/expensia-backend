package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.UpdateExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceExtendedTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private AIServiceClient aiServiceClient;
    @Mock private BudgetAlertService budgetAlertService;
    @Mock private CategoryRepository categoryRepository;
    @Mock private WalletService walletService;

    @InjectMocks
    private ExpenseService expenseService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Expense expense(Long id, BigDecimal amount) {
        Expense e = new Expense();
        e.setExpenseId(id);
        e.setUserId(1L);
        e.setAmount(amount);
        e.setDescription("desc");
        e.setMerchant("merchant");
        e.setCategoryId(3L);
        e.setCategoryName("Food");
        e.setCategoryConfidence(0.9);
        e.setDate(LocalDateTime.now());
        e.setCreatedAt(LocalDateTime.now());
        e.setIsRecurring(false);
        e.setRecurringActive(false);
        e.setCreatedByVoice(false);
        return e;
    }

    private UpdateExpenseRequest updateRequest(BigDecimal amount) {
        UpdateExpenseRequest req = new UpdateExpenseRequest();
        setField(req, "amount", amount);
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

    private AICategorizationResponse aiOther() {
        return new AICategorizationResponse() {
            @Override public boolean isSuccess() { return false; }
            @Override public String getCategory() { return "Other"; }
            @Override public Double getConfidence() { return 0.1; }
        };
    }

    // ── getMyExpenses ─────────────────────────────────────────────────────────

    @Test
    void getMyExpenses_returnsUserExpenses() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(expense(1L, new BigDecimal("50")), expense(2L, new BigDecimal("30"))));

        List<ExpenseResponse> result = expenseService.getMyExpenses();

        assertEquals(2, result.size());
    }

    // ── getExpenseById ────────────────────────────────────────────────────────

    @Test
    void getExpenseById_ownExpense_returnsResponse() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense(1L, new BigDecimal("50"))));

        ExpenseResponse result = expenseService.getExpenseById(1L);

        assertEquals(1L, result.getExpenseId());
    }

    @Test
    void getExpenseById_notFound_throws() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(99L));
    }

    @Test
    void getExpenseById_otherUserExpense_throws() {
        Expense other = expense(1L, new BigDecimal("50"));
        other.setUserId(99L);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(other));

        assertThrows(UnauthorizedException.class, () -> expenseService.getExpenseById(1L));
    }

    // ── deleteExpense ─────────────────────────────────────────────────────────

    @Test
    void deleteExpense_refundsWalletAndDeletes() {
        Expense e = expense(1L, new BigDecimal("75"));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(e));

        expenseService.deleteExpense(1L);

        verify(walletService).increaseSavings(1L, new BigDecimal("75"));
        verify(expenseRepository).delete(e);
    }

    @Test
    void deleteExpense_notFound_throws() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(99L));
    }

    @Test
    void deleteExpense_otherUserExpense_throws() {
        Expense other = expense(1L, new BigDecimal("50"));
        other.setUserId(99L);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(other));

        assertThrows(UnauthorizedException.class, () -> expenseService.deleteExpense(1L));
        verify(expenseRepository, never()).delete(any());
    }

    // ── updateExpense ─────────────────────────────────────────────────────────

    @Test
    void updateExpense_amountIncrease_deductsWalletDifference() {
        Expense existing = expense(1L, new BigDecimal("50"));
        UpdateExpenseRequest req = updateRequest(new BigDecimal("80"));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        expenseService.updateExpense(1L, req);

        verify(walletService).decreaseSavings(1L, new BigDecimal("30"));
    }

    @Test
    void updateExpense_amountDecrease_refundsWalletDifference() {
        Expense existing = expense(1L, new BigDecimal("100"));
        UpdateExpenseRequest req = updateRequest(new BigDecimal("60"));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        expenseService.updateExpense(1L, req);

        verify(walletService).increaseSavings(1L, new BigDecimal("40"));
    }

    @Test
    void updateExpense_samAmount_noWalletChange() {
        Expense existing = expense(1L, new BigDecimal("50"));
        UpdateExpenseRequest req = updateRequest(new BigDecimal("50"));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        expenseService.updateExpense(1L, req);

        verify(walletService, never()).decreaseSavings(any(), any());
        verify(walletService, never()).increaseSavings(any(), any());
    }

    @Test
    void updateExpense_descriptionChange_triggerRecategorization() {
        Expense existing = expense(1L, new BigDecimal("50"));
        UpdateExpenseRequest req = new UpdateExpenseRequest();
        setField(req, "description", "New description");
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(aiServiceClient.categorizeExpense(any(), any())).thenReturn(aiOther());
        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.empty());
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        expenseService.updateExpense(1L, req);

        verify(aiServiceClient).categorizeExpense("New description", "merchant");
    }

    @Test
    void updateExpense_toggleRecurringOff_clearsNextOccurrence() {
        Expense existing = expense(1L, new BigDecimal("50"));
        existing.setIsRecurring(true);
        existing.setFrequency("MONTHLY");
        existing.setNextOccurrence(LocalDateTime.now().plusMonths(1));
        existing.setRecurringActive(true);

        UpdateExpenseRequest req = new UpdateExpenseRequest();
        setField(req, "isRecurring", false);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = expenseService.updateExpense(1L, req);

        assertNull(response.getNextOccurrence());
        assertFalse(response.getRecurringActive());
    }

    @Test
    void updateExpense_notFound_throws() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.updateExpense(99L, new UpdateExpenseRequest()));
    }
}
