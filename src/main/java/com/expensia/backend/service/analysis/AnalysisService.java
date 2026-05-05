package com.expensia.backend.service.analysis;

import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public BigDecimal getMonthlyExpenses(Long userId, int year, int month) {
        LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        return expenseRepository.sumByUserIdAndDateBetween(userId, start, end);
    }

    public BigDecimal getMonthlyIncome(Long userId, int year, int month) {
        LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        return incomeRepository.sumByUserIdAndDateBetween(userId, start, end);
    }

    public BigDecimal getBalance(Long userId, int year, int month) {
        BigDecimal income = getMonthlyIncome(userId, year, month);
        BigDecimal expenses = getMonthlyExpenses(userId, year, month);
        return income.subtract(expenses);
    }
}
