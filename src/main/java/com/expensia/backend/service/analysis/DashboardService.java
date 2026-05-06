package com.expensia.backend.service.analysis;

import com.expensia.backend.dto.response.DashboardResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardService(IncomeRepository incomeRepository,
                            ExpenseRepository expenseRepository,
                            BudgetRepository budgetRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public DashboardResponse getSummary() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        BigDecimal totalIncome = incomeRepository.sumByUserIdAndDateBetween(
                currentUser.getUserId(),
                start,
                end
        );

        BigDecimal totalExpenses = expenseRepository.sumByUserIdAndDateBetween(
                currentUser.getUserId(),
                start,
                end
        );

        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        int totalBudgets = budgetRepository.findByUserId(currentUser.getUserId()).size();

        return new DashboardResponse(
                totalIncome,
                totalExpenses,
                currentBalance,
                totalBudgets
        );
    }
}
