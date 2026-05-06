package com.expensia.backend.dto.response;

import java.math.BigDecimal;

public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private int totalBudgets;

    public DashboardResponse(BigDecimal totalIncome, BigDecimal totalExpenses,
                             BigDecimal currentBalance, int totalBudgets) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = currentBalance;
        this.totalBudgets = totalBudgets;
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

    public int getTotalBudgets() {
        return totalBudgets;
    }
}