package com.expensia.backend.dto;

import com.expensia.backend.dto.response.*;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.model.enums.RiskPreference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

    @Test
    void authResponse_constructorAndGetters() {
        AuthResponse r = new AuthResponse("token", 1L, "user@example.com", "Ahmed", RiskPreference.HIGH);
        assertEquals("token", r.getToken());
        assertEquals(1L, r.getUserId());
        assertEquals("user@example.com", r.getEmail());
        assertEquals("Ahmed", r.getName());
        assertEquals(RiskPreference.HIGH, r.getRiskPreference());
    }

    @Test
    void expenseResponse_constructorAndGetters() {
        ExpenseResponse r = new ExpenseResponse(
                1L, 2L, new BigDecimal("50"), 3L, "Food", 0.9,
                now, "Lunch", "Rest", "CASH", false, null, null, false, false, now
        );
        assertEquals(1L, r.getExpenseId());
        assertEquals(2L, r.getUserId());
        assertEquals(new BigDecimal("50"), r.getAmount());
        assertEquals(3L, r.getCategoryId());
        assertEquals("Food", r.getCategoryName());
        assertEquals(0.9, r.getCategoryConfidence());
        assertEquals("Lunch", r.getDescription());
        assertEquals("Rest", r.getMerchant());
        assertEquals("CASH", r.getPaymentMethod());
        assertFalse(r.getIsRecurring());
        assertNull(r.getFrequency());
        assertNull(r.getNextOccurrence());
        assertFalse(r.getRecurringActive());
        assertFalse(r.getCreatedByVoice());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void incomeResponse_constructorAndGetters() {
        IncomeResponse r = new IncomeResponse(
                1L, 2L, new BigDecimal("3000"), now, "Salary", "MONTHLY", true, now.plusMonths(1), true
        );
        assertEquals(1L, r.getIncomeId());
        assertEquals(2L, r.getUserId());
        assertEquals(new BigDecimal("3000"), r.getAmount());
        assertEquals("Salary", r.getSource());
        assertEquals("MONTHLY", r.getFrequency());
        assertTrue(r.getIsRecurring());
        assertTrue(r.getRecurringActive());
        assertNotNull(r.getNextOccurrence());
    }

    @Test
    void budgetResponse_constructorAndGetters() {
        BudgetResponse r = new BudgetResponse(
                1L, 2L, 3L, new BigDecimal("500"), new BigDecimal("200"), 0.8, false, today, today.plusMonths(1)
        );
        assertEquals(1L, r.getBudgetId());
        assertEquals(2L, r.getUserId());
        assertEquals(3L, r.getCategoryId());
        assertEquals(new BigDecimal("500"), r.getLimitAmount());
        assertEquals(new BigDecimal("200"), r.getSpentAmount());
        assertEquals(0.8, r.getAlertThreshold());
        assertFalse(r.getIsOverBudget());
        assertNotNull(r.getStartDate());
        assertNotNull(r.getEndDate());
    }

    @Test
    void goalResponse_constructorAndGetters() {
        GoalResponse r = new GoalResponse(
                1L, 2L, "Car", new BigDecimal("10000"), new BigDecimal("2000"),
                today.plusYears(1), GoalStatus.ACTIVE, now
        );
        assertEquals(1L, r.getGoalId());
        assertEquals(2L, r.getUserId());
        assertEquals("Car", r.getName());
        assertEquals(new BigDecimal("10000"), r.getTargetAmount());
        assertEquals(new BigDecimal("2000"), r.getCurrentAmount());
        assertEquals(GoalStatus.ACTIVE, r.getStatus());
        assertNotNull(r.getDeadline());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void walletResponse_constructorAndGetters() {
        WalletResponse r = new WalletResponse(1L, new BigDecimal("1500"), now);
        assertEquals(1L, r.getWalletId());
        assertEquals(new BigDecimal("1500"), r.getCurrentSavings());
        assertNotNull(r.getUpdatedAt());
    }

    @Test
    void notificationResponse_constructorAndGetters() {
        NotificationResponse r = new NotificationResponse(
                1L, 2L, "Budget Warning", "You spent 80%",
                NotificationType.BUDGET_WARNING, false, now
        );
        assertEquals(1L, r.getNotificationId());
        assertEquals(2L, r.getUserId());
        assertEquals("Budget Warning", r.getTitle());
        assertEquals("You spent 80%", r.getMessage());
        assertEquals(NotificationType.BUDGET_WARNING, r.getType());
        assertFalse(r.getIsRead());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void userResponse_settersAndGetters() {
        UserResponse r = new UserResponse();
        r.setUserId(1L);
        r.setEmail("user@example.com");
        r.setName("Ahmed");
        r.setPhone("0123456789");
        r.setRiskPreference(RiskPreference.LOW);
        r.setCreatedAt(now);

        assertEquals(1L, r.getUserId());
        assertEquals("user@example.com", r.getEmail());
        assertEquals("Ahmed", r.getName());
        assertEquals("0123456789", r.getPhone());
        assertEquals(RiskPreference.LOW, r.getRiskPreference());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void dashboardResponse_constructorAndGetters() {
        DashboardResponse r = new DashboardResponse(
                new BigDecimal("5000"), new BigDecimal("2000"),
                new BigDecimal("3000"), new BigDecimal("1500"), 3, 2
        );
        assertEquals(new BigDecimal("5000"), r.getTotalIncome());
        assertEquals(new BigDecimal("2000"), r.getTotalExpenses());
        assertEquals(new BigDecimal("3000"), r.getCurrentBalance());
        assertEquals(new BigDecimal("1500"), r.getCurrentSavings());
        assertEquals(3, r.getTotalBudgets());
        assertEquals(2, r.getActiveGoals());
    }

    @Test
    void aiRecommendationResponse_settersAndGetters() {
        AIRecommendationResponse r = new AIRecommendationResponse();
        assertNull(r.getSpendingInsights());
        assertNull(r.getSavingRecommendations());
        assertNull(r.getInvestmentSuggestions());
        assertNull(r.getGoalPlans());
        assertNull(r.getOverallScore());
        assertFalse(r.isSuccess());
    }
}
