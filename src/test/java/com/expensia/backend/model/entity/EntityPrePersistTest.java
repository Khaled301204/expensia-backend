package com.expensia.backend.model.entity;

import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.model.enums.RiskPreference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityPrePersistTest {

    // ─── Expense ─────────────────────────────────────────────────────────────

    @Test
    void expense_prePersist_setsDefaultsWhenNull() {
        Expense e = new Expense();
        e.prePersist();

        assertNotNull(e.getDate());
        assertNotNull(e.getCreatedAt());
        assertFalse(e.getIsRecurring());
        assertFalse(e.getCreatedByVoice());
        assertFalse(e.getRecurringActive());
    }

    @Test
    void expense_prePersist_doesNotOverwriteExistingValues() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 15, 10, 0);
        Expense e = new Expense();
        e.setDate(date);
        e.setIsRecurring(true);
        e.setRecurringActive(true);
        e.setCreatedByVoice(true);
        e.prePersist();

        assertEquals(date, e.getDate());
        assertTrue(e.getIsRecurring());
        assertTrue(e.getRecurringActive());
        assertTrue(e.getCreatedByVoice());
    }

    @Test
    void expense_prePersist_calculatesNextOccurrenceWhenRecurring() {
        LocalDateTime date = LocalDateTime.of(2026, 6, 1, 9, 0);
        Expense e = new Expense();
        e.setDate(date);
        e.setIsRecurring(true);
        e.setFrequency("MONTHLY");
        e.prePersist();

        assertEquals(date.plusMonths(1), e.getNextOccurrence());
    }

    @Test
    void expense_prePersist_noNextOccurrenceWhenNotRecurring() {
        Expense e = new Expense();
        e.setIsRecurring(false);
        e.prePersist();

        assertNull(e.getNextOccurrence());
    }

    @Test
    void expense_allSettersAndGetters() {
        Expense e = new Expense();
        LocalDateTime now = LocalDateTime.now();
        e.setExpenseId(1L);
        e.setUserId(2L);
        e.setAmount(new BigDecimal("99.99"));
        e.setCategoryId(3L);
        e.setCategoryName("Food");
        e.setCategoryConfidence(0.85);
        e.setDate(now);
        e.setCreatedAt(now);
        e.setDescription("Lunch");
        e.setMerchant("Restaurant");
        e.setPaymentMethod("CASH");
        e.setIsRecurring(true);
        e.setFrequency("WEEKLY");
        e.setNextOccurrence(now.plusWeeks(1));
        e.setRecurringActive(true);
        e.setCreatedByVoice(false);

        assertEquals(1L, e.getExpenseId());
        assertEquals(2L, e.getUserId());
        assertEquals(new BigDecimal("99.99"), e.getAmount());
        assertEquals(3L, e.getCategoryId());
        assertEquals("Food", e.getCategoryName());
        assertEquals(0.85, e.getCategoryConfidence());
        assertEquals(now, e.getDate());
        assertEquals("Lunch", e.getDescription());
        assertEquals("Restaurant", e.getMerchant());
        assertEquals("CASH", e.getPaymentMethod());
        assertTrue(e.getIsRecurring());
        assertEquals("WEEKLY", e.getFrequency());
        assertTrue(e.getRecurringActive());
        assertFalse(e.getCreatedByVoice());
        assertNotNull(e.toString());
    }

    // ─── Income ──────────────────────────────────────────────────────────────

    @Test
    void income_prePersist_setsDefaultsWhenNull() {
        Income i = new Income();
        i.prePersist();

        assertNotNull(i.getDate());
        assertFalse(i.getIsRecurring());
        assertFalse(i.getRecurringActive());
    }

    @Test
    void income_prePersist_calculatesNextOccurrenceWhenRecurring() {
        LocalDateTime date = LocalDateTime.of(2026, 7, 1, 0, 0);
        Income i = new Income();
        i.setDate(date);
        i.setIsRecurring(true);
        i.setFrequency("WEEKLY");
        i.prePersist();

        assertEquals(date.plusWeeks(1), i.getNextOccurrence());
    }

    @Test
    void income_allSettersAndGetters() {
        Income i = new Income();
        LocalDateTime now = LocalDateTime.now();
        i.setIncomeId(1L);
        i.setUserId(2L);
        i.setAmount(new BigDecimal("3000"));
        i.setDate(now);
        i.setSource("Salary");
        i.setFrequency("MONTHLY");
        i.setIsRecurring(true);
        i.setNextOccurrence(now.plusMonths(1));
        i.setRecurringActive(true);

        assertEquals(1L, i.getIncomeId());
        assertEquals(2L, i.getUserId());
        assertEquals(new BigDecimal("3000"), i.getAmount());
        assertEquals("Salary", i.getSource());
        assertEquals("MONTHLY", i.getFrequency());
        assertTrue(i.getIsRecurring());
        assertTrue(i.getRecurringActive());
        assertNotNull(i.toString());
    }

    // ─── SavingGoal ──────────────────────────────────────────────────────────

    @Test
    void savingGoal_prePersist_setsDefaults() {
        SavingGoal g = new SavingGoal();
        g.prePersist();

        assertEquals(BigDecimal.ZERO, g.getCurrentAmount());
        assertEquals(GoalStatus.ACTIVE, g.getStatus());
        assertNotNull(g.getCreatedAt());
    }

    @Test
    void savingGoal_prePersist_doesNotOverwriteExisting() {
        SavingGoal g = new SavingGoal();
        g.setCurrentAmount(new BigDecimal("500"));
        g.setStatus(GoalStatus.COMPLETED);
        g.prePersist();

        assertEquals(new BigDecimal("500"), g.getCurrentAmount());
        assertEquals(GoalStatus.COMPLETED, g.getStatus());
    }

    @Test
    void savingGoal_allSettersAndGetters() {
        SavingGoal g = new SavingGoal();
        LocalDate deadline = LocalDate.of(2027, 1, 1);
        LocalDateTime now = LocalDateTime.now();
        g.setGoalId(1L);
        g.setUserId(2L);
        g.setName("Car");
        g.setTargetAmount(new BigDecimal("10000"));
        g.setCurrentAmount(new BigDecimal("2000"));
        g.setDeadline(deadline);
        g.setStatus(GoalStatus.ACTIVE);
        g.setCreatedAt(now);

        assertEquals(1L, g.getGoalId());
        assertEquals("Car", g.getName());
        assertEquals(new BigDecimal("10000"), g.getTargetAmount());
        assertEquals(new BigDecimal("2000"), g.getCurrentAmount());
        assertEquals(deadline, g.getDeadline());
        assertEquals(GoalStatus.ACTIVE, g.getStatus());
        assertNotNull(g.toString());
    }

    // ─── Budget ──────────────────────────────────────────────────────────────

    @Test
    void budget_prePersist_setsDefaultThreshold() {
        Budget b = new Budget();
        b.prePersist();

        assertEquals(0.80, b.getAlertThreshold());
    }

    @Test
    void budget_allSettersAndGetters() {
        Budget b = new Budget();
        b.setBudgetId(1L);
        b.setUserId(2L);
        b.setCategoryId(3L);
        b.setLimitAmount(new BigDecimal("500"));
        b.setStartDate(LocalDate.of(2026, 7, 1));
        b.setEndDate(LocalDate.of(2026, 7, 31));
        b.setAlertThreshold(0.9);

        assertEquals(1L, b.getBudgetId());
        assertEquals(2L, b.getUserId());
        assertEquals(3L, b.getCategoryId());
        assertEquals(new BigDecimal("500"), b.getLimitAmount());
        assertEquals(0.9, b.getAlertThreshold());
        assertNotNull(b.toString());
    }

    // ─── Wallet ──────────────────────────────────────────────────────────────

    @Test
    void wallet_updateTimestamp_setsUpdatedAt() {
        Wallet w = new Wallet();
        w.setCurrentSavings(new BigDecimal("100"));
        w.updateTimestamp();

        assertNotNull(w.getUpdatedAt());
    }

    @Test
    void wallet_updateTimestamp_setsZeroWhenSavingsNull() {
        Wallet w = new Wallet();
        w.updateTimestamp();

        assertEquals(BigDecimal.ZERO, w.getCurrentSavings());
        assertNotNull(w.getUpdatedAt());
    }

    @Test
    void wallet_allSettersAndGetters() {
        Wallet w = new Wallet();
        w.setWalletId(1L);
        w.setUserId(2L);
        w.setCurrentSavings(new BigDecimal("1500"));

        assertEquals(1L, w.getWalletId());
        assertEquals(2L, w.getUserId());
        assertEquals(new BigDecimal("1500"), w.getCurrentSavings());
        assertNotNull(w.toString());
    }

    // ─── Notification ────────────────────────────────────────────────────────

    @Test
    void notification_prePersist_setsDefaults() {
        Notification n = new Notification();
        n.prePersist();

        assertFalse(n.getIsRead());
        assertNotNull(n.getCreatedAt());
    }

    @Test
    void notification_allSettersAndGetters() {
        Notification n = new Notification();
        LocalDateTime now = LocalDateTime.now();
        n.setNotificationId(1L);
        n.setUserId(2L);
        n.setTitle("Budget Warning");
        n.setMessage("You spent 80% of your budget");
        n.setType(NotificationType.BUDGET_WARNING);
        n.setIsRead(false);
        n.setCreatedAt(now);

        assertEquals(1L, n.getNotificationId());
        assertEquals(2L, n.getUserId());
        assertEquals("Budget Warning", n.getTitle());
        assertEquals("You spent 80% of your budget", n.getMessage());
        assertEquals(NotificationType.BUDGET_WARNING, n.getType());
        assertFalse(n.getIsRead());
        assertNotNull(n.toString());
    }

    // ─── User ────────────────────────────────────────────────────────────────

    @Test
    void user_builder_setsAllFields() {
        User u = User.builder()
                .userId(1L)
                .email("user@example.com")
                .passwordHash("hashed")
                .name("Ahmed")
                .phone("01234567890")
                .riskPreference(RiskPreference.HIGH)
                .build();

        assertEquals(1L, u.getUserId());
        assertEquals("user@example.com", u.getEmail());
        assertEquals("hashed", u.getPasswordHash());
        assertEquals("Ahmed", u.getName());
        assertEquals("01234567890", u.getPhone());
        assertEquals(RiskPreference.HIGH, u.getRiskPreference());
        assertNotNull(u.toString());
    }

    @Test
    void user_allArgsConstructor() {
        User u = new User(1L, "a@b.com", "hash", "Name", "123", RiskPreference.LOW, null);

        assertEquals("a@b.com", u.getEmail());
        assertEquals(RiskPreference.LOW, u.getRiskPreference());
    }

    @Test
    void user_noArgsAndSetters() {
        User u = new User();
        u.setUserId(5L);
        u.setEmail("x@y.com");
        u.setName("Test");
        u.setPhone("999");
        u.setRiskPreference(RiskPreference.MEDIUM);

        assertEquals(5L, u.getUserId());
        assertEquals("x@y.com", u.getEmail());
        assertEquals(RiskPreference.MEDIUM, u.getRiskPreference());
    }

    // ─── Category ────────────────────────────────────────────────────────────

    @Test
    void category_allSettersAndGetters() {
        Category c = new Category();
        c.setCategoryId(1L);
        c.setName("Food & Dining");
        c.setIcon("🍕");
        c.setColor("#FF5733");

        assertEquals(1L, c.getCategoryId());
        assertEquals("Food & Dining", c.getName());
        assertEquals("🍕", c.getIcon());
        assertEquals("#FF5733", c.getColor());
        assertNotNull(c.toString());
        Category c2 = new Category();
        c2.setCategoryId(1L);
        c2.setName("Food & Dining");
        c2.setIcon("🍕");
        c2.setColor("#FF5733");
        assertEquals(c, c2);
    }
}
