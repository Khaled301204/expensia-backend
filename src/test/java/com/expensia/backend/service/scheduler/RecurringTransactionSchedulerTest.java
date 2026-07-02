package com.expensia.backend.service.scheduler;

import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.service.budget.BudgetAlertService;
import com.expensia.backend.service.wallet.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionSchedulerTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private BudgetAlertService budgetAlertService;

    @InjectMocks
    private RecurringTransactionScheduler scheduler;

    private LocalDateTime past(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    private Expense recurringExpense(LocalDateTime nextOccurrence, String frequency) {
        Expense e = new Expense();
        e.setExpenseId(1L);
        e.setUserId(10L);
        e.setAmount(new BigDecimal("100"));
        e.setDescription("Netflix");
        e.setCategoryId(5L);
        e.setCategoryName("Entertainment");
        e.setIsRecurring(true);
        e.setRecurringActive(true);
        e.setFrequency(frequency);
        e.setNextOccurrence(nextOccurrence);
        return e;
    }

    private Income recurringIncome(LocalDateTime nextOccurrence, String frequency) {
        Income i = new Income();
        i.setIncomeId(1L);
        i.setUserId(10L);
        i.setAmount(new BigDecimal("3000"));
        i.setSource("Salary");
        i.setIsRecurring(true);
        i.setRecurringActive(true);
        i.setFrequency(frequency);
        i.setNextOccurrence(nextOccurrence);
        return i;
    }

    @Test
    void processRecurringExpenses_dueExpense_createsGeneratedCopyAndAdvancesNextOccurrence() {
        LocalDateTime dueDate = past(5);
        Expense original = recurringExpense(dueDate, "MONTHLY");

        when(expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, times(2)).save(captor.capture());

        List<Expense> saved = captor.getAllValues();
        Expense generated = saved.get(0);
        Expense updatedOriginal = saved.get(1);

        assertFalse(generated.getIsRecurring());
        assertFalse(generated.getRecurringActive());
        assertNull(generated.getNextOccurrence());
        assertEquals(dueDate, generated.getDate());
        assertEquals(new BigDecimal("100"), generated.getAmount());

        assertNotNull(updatedOriginal.getNextOccurrence());
        assertTrue(updatedOriginal.getNextOccurrence().isAfter(dueDate));
    }

    @Test
    void processRecurringExpenses_dueExpense_decreasesWalletAndChecksBudget() {
        Expense original = recurringExpense(past(2), "WEEKLY");
        when(expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        verify(walletService).decreaseSavings(eq(10L), eq(new BigDecimal("100")));
        verify(budgetAlertService).checkBudgetAlert(eq(10L), any(Expense.class));
    }

    @Test
    void processRecurringExpenses_noDueExpenses_doesNothing() {
        when(expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of());
        when(incomeRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of());

        scheduler.processRecurringTransactions();

        verify(expenseRepository, never()).save(any());
        verify(walletService, never()).decreaseSavings(any(), any());
    }

    @Test
    void processRecurringIncomes_dueIncome_createsGeneratedCopyAndAdvancesNextOccurrence() {
        LocalDateTime dueDate = past(3);
        Income original = recurringIncome(dueDate, "MONTHLY");

        when(incomeRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository, times(2)).save(captor.capture());

        List<Income> saved = captor.getAllValues();
        Income generated = saved.get(0);
        Income updatedOriginal = saved.get(1);

        assertFalse(generated.getIsRecurring());
        assertFalse(generated.getRecurringActive());
        assertNull(generated.getNextOccurrence());
        assertEquals(dueDate, generated.getDate());

        assertNotNull(updatedOriginal.getNextOccurrence());
        assertTrue(updatedOriginal.getNextOccurrence().isAfter(dueDate));
    }

    @Test
    void processRecurringIncomes_dueIncome_increasesWallet() {
        Income original = recurringIncome(past(1), "WEEKLY");
        when(incomeRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        verify(walletService).increaseSavings(eq(10L), eq(new BigDecimal("3000")));
    }

    @Test
    void processRecurring_multipleMissedCycles_createsOneEntryPerCycle() {
        LocalDateTime twoCyclesAgo = LocalDateTime.now().minusWeeks(2).minusDays(1);
        Expense original = recurringExpense(twoCyclesAgo, "WEEKLY");

        when(expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, atLeast(3)).save(captor.capture());

        long generatedCount = captor.getAllValues().stream()
                .filter(e -> Boolean.FALSE.equals(e.getIsRecurring()))
                .count();
        assertTrue(generatedCount >= 2);
    }

    @Test
    void processRecurring_nextOccurrenceAdvancedCorrectly_forDailyFrequency() {
        // Use 2 hours ago so +1 day = 22 hours from now, clearly in the future (loop runs exactly once)
        LocalDateTime yesterday = LocalDateTime.now().minusHours(2);
        Expense original = recurringExpense(yesterday, "DAILY");

        when(expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(any()))
                .thenReturn(List.of(original));
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.processRecurringTransactions();

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, times(2)).save(captor.capture());

        Expense updatedOriginal = captor.getAllValues().get(1);
        LocalDateTime expected = yesterday.plusDays(1);
        assertEquals(expected, updatedOriginal.getNextOccurrence());
    }
}
