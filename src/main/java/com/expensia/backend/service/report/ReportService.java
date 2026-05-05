//package com.expensia.backend.service.report;
//
//import com.expensia.backend.dto.response.ReportResponse;
//import com.expensia.backend.model.entity.Expense;
//import com.expensia.backend.repository.ExpenseRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ReportService {
//
//    private final AnalysisService analysisService;
//    private final ExpenseRepository expenseRepository;
//
//    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
//        BigDecimal income = analysisService.getMonthlyIncome(userId, year, month);
//        BigDecimal expenses = analysisService.getMonthlyExpenses(userId, year, month);
//        BigDecimal balance = income.subtract(expenses);
//
//        // Category breakdown
//        LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
//        LocalDateTime end = start.plusMonths(1);
//        List<Expense> monthExpenses = expenseRepository
//                .findByUserIdAndDateBetween(userId, start, end);
//
//        Map<String, BigDecimal> categoryBreakdown = monthExpenses.stream()
//                .collect(Collectors.groupingBy(
//                        e -> "Category_" + e.getCategoryId(),
//                        Collectors.reducing(BigDecimal.ZERO,
//                                Expense::getAmount,
//                                BigDecimal::add)
//                ));
//
//        return new MonthlyReportResponse(income, expenses, balance, categoryBreakdown);
//    }
//}
