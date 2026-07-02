package com.expensia.backend.service.budget;

import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetAlertServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BudgetAlertService budgetAlertService;

    private Budget budget(BigDecimal limit, Double threshold) {
        Budget b = new Budget();
        b.setBudgetId(1L);
        b.setUserId(1L);
        b.setCategoryId(3L);
        b.setLimitAmount(limit);
        b.setStartDate(LocalDate.of(2026, 7, 1));
        b.setEndDate(LocalDate.of(2026, 7, 31));
        b.setAlertThreshold(threshold);
        return b;
    }

    private Expense expense(Long categoryId) {
        Expense e = new Expense();
        e.setExpenseId(10L);
        e.setUserId(1L);
        e.setCategoryId(categoryId);
        e.setAmount(new BigDecimal("50"));
        return e;
    }

    @Test
    void checkBudgetAlert_noBudgetForCategory_doesNothing() {
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of());

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    void checkBudgetAlert_nullCategoryId_doesNothing() {
        budgetAlertService.checkBudgetAlert(1L, expense(null));

        verify(budgetRepository, never()).findByUserIdAndCategoryId(any(), any());
    }

    @Test
    void checkBudgetAlert_belowThreshold_noAlert() {
        Budget b = budget(new BigDecimal("500"), 0.8);
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of(b));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("300"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryNamed("Food")));

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    void checkBudgetAlert_atThreshold_createsWarning() {
        Budget b = budget(new BigDecimal("500"), 0.8);
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of(b));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("400"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryNamed("Food")));
        when(notificationService.hasUnreadNotificationContaining(1L, NotificationType.BUDGET_WARNING, "Food"))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService).createNotification(
                eq(1L),
                eq("Budget warning"),
                contains("Food"),
                eq(NotificationType.BUDGET_WARNING)
        );
    }

    @Test
    void checkBudgetAlert_exceeded_createsExceededAlert() {
        Budget b = budget(new BigDecimal("500"), 0.8);
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of(b));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("600"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryNamed("Food")));
        when(notificationService.hasUnreadNotificationContaining(1L, NotificationType.BUDGET_EXCEEDED, "Food"))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService).createNotification(
                eq(1L),
                eq("Budget exceeded"),
                contains("Food"),
                eq(NotificationType.BUDGET_EXCEEDED)
        );
    }

    @Test
    void checkBudgetAlert_exceededAlreadyNotified_doesNotDuplicate() {
        Budget b = budget(new BigDecimal("500"), 0.8);
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of(b));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("600"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryNamed("Food")));
        when(notificationService.hasUnreadNotificationContaining(1L, NotificationType.BUDGET_EXCEEDED, "Food"))
                .thenReturn(true);

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    void checkBudgetAlert_warningAlreadyNotified_doesNotDuplicate() {
        Budget b = budget(new BigDecimal("500"), 0.8);
        when(budgetRepository.findByUserIdAndCategoryId(1L, 3L)).thenReturn(List.of(b));
        when(expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("400"));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryNamed("Food")));
        when(notificationService.hasUnreadNotificationContaining(1L, NotificationType.BUDGET_WARNING, "Food"))
                .thenReturn(true);

        budgetAlertService.checkBudgetAlert(1L, expense(3L));

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    private Category categoryNamed(String name) {
        Category c = new Category();
        c.setCategoryId(3L);
        c.setName(name);
        return c;
    }
}
