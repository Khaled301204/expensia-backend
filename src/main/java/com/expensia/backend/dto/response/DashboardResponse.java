package com.expensia.backend.dto.response;

import java.math.BigDecimal;

public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private BigDecimal currentSavings;

    private int totalBudgets;
    private int activeGoals;

    public DashboardResponse(
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal currentBalance,
            BigDecimal currentSavings,
            int totalBudgets,
            int activeGoals
    ) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = currentBalance;
        this.currentSavings = currentSavings;
        this.totalBudgets = totalBudgets;
        this.activeGoals = activeGoals;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getCurrentSavings() {
        return currentSavings;
    }

    public int getTotalBudgets() {
        return totalBudgets;
    }

    public int getActiveGoals() {
        return activeGoals;
    }
}