package com.expensia.backend.service.scheduler;

import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.service.budget.BudgetAlertService;
import com.expensia.backend.service.wallet.WalletService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecurringTransactionScheduler {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final WalletService walletService;
    private final BudgetAlertService budgetAlertService;

    public RecurringTransactionScheduler(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            WalletService walletService,
            BudgetAlertService budgetAlertService
    ) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.walletService = walletService;
        this.budgetAlertService = budgetAlertService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {
        LocalDateTime now = LocalDateTime.now();

        processRecurringIncomes(now);
        processRecurringExpenses(now);
    }

    private void processRecurringIncomes(LocalDateTime now) {
        List<Income> dueIncomes =
                incomeRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(now);

        for (Income original : dueIncomes) {
            while (original.getNextOccurrence() != null &&
                    !original.getNextOccurrence().isAfter(now)) {

                Income generated = new Income();
                generated.setUserId(original.getUserId());
                generated.setAmount(original.getAmount());
                generated.setDate(original.getNextOccurrence());
                generated.setSource(original.getSource());
                generated.setFrequency(original.getFrequency());
                generated.setIsRecurring(false);
                generated.setRecurringActive(false);
                generated.setNextOccurrence(null);

                incomeRepository.save(generated);

                walletService.increaseSavings(
                        original.getUserId(),
                        original.getAmount()
                );

                original.setNextOccurrence(
                        calculateNextOccurrence(
                                original.getNextOccurrence(),
                                original.getFrequency()
                        )
                );
            }

            incomeRepository.save(original);
        }
    }

    private void processRecurringExpenses(LocalDateTime now) {
        List<Expense> dueExpenses =
                expenseRepository.findByIsRecurringTrueAndRecurringActiveTrueAndNextOccurrenceLessThanEqual(now);

        for (Expense original : dueExpenses) {
            while (original.getNextOccurrence() != null &&
                    !original.getNextOccurrence().isAfter(now)) {

                Expense generated = new Expense();
                generated.setUserId(original.getUserId());
                generated.setAmount(original.getAmount());
                generated.setDate(original.getNextOccurrence());
                generated.setDescription(original.getDescription());
                generated.setMerchant(original.getMerchant());
                generated.setPaymentMethod(original.getPaymentMethod());
                generated.setCategoryId(original.getCategoryId());
                generated.setCategoryName(original.getCategoryName());
                generated.setCategoryConfidence(original.getCategoryConfidence());
                generated.setFrequency(original.getFrequency());
                generated.setIsRecurring(false);
                generated.setRecurringActive(false);
                generated.setCreatedByVoice(false);
                generated.setNextOccurrence(null);

                Expense savedExpense = expenseRepository.save(generated);

                walletService.decreaseSavings(
                        original.getUserId(),
                        original.getAmount()
                );

                budgetAlertService.checkBudgetAlert(
                        original.getUserId(),
                        savedExpense
                );

                original.setNextOccurrence(
                        calculateNextOccurrence(
                                original.getNextOccurrence(),
                                original.getFrequency()
                        )
                );
            }

            expenseRepository.save(original);
        }
    }

    private LocalDateTime calculateNextOccurrence(
            LocalDateTime from,
            String frequency
    ) {
        if (from == null || frequency == null) {
            return null;
        }

        return switch (frequency.toUpperCase()) {
            case "DAILY" -> from.plusDays(1);
            case "WEEKLY" -> from.plusWeeks(1);
            case "MONTHLY" -> from.plusMonths(1);
            case "YEARLY" -> from.plusYears(1);
            default -> null;
        };
    }
}