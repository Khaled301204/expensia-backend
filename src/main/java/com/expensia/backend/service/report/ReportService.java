package com.expensia.backend.service.report;

import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public ReportService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    public ReportResponse generateMonthlyReport() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        LocalDateTime start =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses =
                expenseRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        List<Income> incomes =
                incomeRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Expense expense : expenses) {

            totalExpenses = totalExpenses.add(expense.getAmount());

            String category =
                    expense.getCategoryName() != null
                            ? expense.getCategoryName()
                            : "Other";

            categoryBreakdown.put(
                    category,
                    categoryBreakdown.getOrDefault(category, BigDecimal.ZERO)
                            .add(expense.getAmount())
            );
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new ReportResponse(
                totalIncome,
                totalExpenses,
                balance,
                categoryBreakdown
        );
    }
}