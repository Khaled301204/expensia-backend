package com.expensia.backend.model.entity;

import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.model.enums.RiskPreference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityEqualsHashCodeTest {

    @Test
    void expense_equalWhenSameFields() {
        LocalDateTime now = LocalDateTime.now();
        Expense a = new Expense();
        a.setExpenseId(1L); a.setUserId(1L); a.setAmount(new BigDecimal("50")); a.setDate(now);
        a.setDescription("d"); a.setMerchant("m"); a.setPaymentMethod("CASH");
        a.setIsRecurring(false); a.setRecurringActive(false); a.setCreatedByVoice(false);

        Expense b = new Expense();
        b.setExpenseId(1L); b.setUserId(1L); b.setAmount(new BigDecimal("50")); b.setDate(now);
        b.setDescription("d"); b.setMerchant("m"); b.setPaymentMethod("CASH");
        b.setIsRecurring(false); b.setRecurringActive(false); b.setCreatedByVoice(false);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void expense_notEqualWhenDifferentId() {
        Expense a = new Expense(); a.setExpenseId(1L);
        Expense b = new Expense(); b.setExpenseId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void income_equalWhenSameFields() {
        LocalDateTime now = LocalDateTime.now();
        Income a = new Income();
        a.setIncomeId(1L); a.setUserId(1L); a.setAmount(new BigDecimal("3000"));
        a.setDate(now); a.setSource("Salary"); a.setIsRecurring(false); a.setRecurringActive(false);

        Income b = new Income();
        b.setIncomeId(1L); b.setUserId(1L); b.setAmount(new BigDecimal("3000"));
        b.setDate(now); b.setSource("Salary"); b.setIsRecurring(false); b.setRecurringActive(false);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void budget_equalWhenSameFields() {
        Budget a = new Budget();
        a.setBudgetId(1L); a.setUserId(1L); a.setCategoryId(3L);
        a.setLimitAmount(new BigDecimal("500")); a.setAlertThreshold(0.8);
        a.setStartDate(LocalDate.of(2026, 7, 1)); a.setEndDate(LocalDate.of(2026, 7, 31));

        Budget b = new Budget();
        b.setBudgetId(1L); b.setUserId(1L); b.setCategoryId(3L);
        b.setLimitAmount(new BigDecimal("500")); b.setAlertThreshold(0.8);
        b.setStartDate(LocalDate.of(2026, 7, 1)); b.setEndDate(LocalDate.of(2026, 7, 31));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void savingGoal_equalWhenSameFields() {
        LocalDateTime now = LocalDateTime.now();
        SavingGoal a = new SavingGoal();
        a.setGoalId(1L); a.setUserId(1L); a.setName("Car");
        a.setTargetAmount(new BigDecimal("10000")); a.setCurrentAmount(BigDecimal.ZERO);
        a.setDeadline(LocalDate.now()); a.setStatus(GoalStatus.ACTIVE); a.setCreatedAt(now);

        SavingGoal b = new SavingGoal();
        b.setGoalId(1L); b.setUserId(1L); b.setName("Car");
        b.setTargetAmount(new BigDecimal("10000")); b.setCurrentAmount(BigDecimal.ZERO);
        b.setDeadline(LocalDate.now()); b.setStatus(GoalStatus.ACTIVE); b.setCreatedAt(now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void wallet_equalWhenSameFields() {
        Wallet a = new Wallet(); a.setWalletId(1L); a.setUserId(1L); a.setCurrentSavings(new BigDecimal("500"));
        Wallet b = new Wallet(); b.setWalletId(1L); b.setUserId(1L); b.setCurrentSavings(new BigDecimal("500"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notification_equalWhenSameFields() {
        LocalDateTime now = LocalDateTime.now();
        Notification a = new Notification();
        a.setNotificationId(1L); a.setUserId(1L); a.setTitle("T"); a.setMessage("M");
        a.setType(NotificationType.GENERAL); a.setIsRead(false); a.setCreatedAt(now);

        Notification b = new Notification();
        b.setNotificationId(1L); b.setUserId(1L); b.setTitle("T"); b.setMessage("M");
        b.setType(NotificationType.GENERAL); b.setIsRead(false); b.setCreatedAt(now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void user_equalsAndHashCode() {
        User a = User.builder().userId(1L).email("a@b.com").name("A").riskPreference(RiskPreference.LOW).build();
        User b = User.builder().userId(1L).email("a@b.com").name("A").riskPreference(RiskPreference.LOW).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void category_equalsAndHashCode() {
        Category a = new Category(); a.setCategoryId(1L); a.setName("Food"); a.setIcon("x"); a.setColor("y");
        Category b = new Category(); b.setCategoryId(1L); b.setName("Food"); b.setIcon("x"); b.setColor("y");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
