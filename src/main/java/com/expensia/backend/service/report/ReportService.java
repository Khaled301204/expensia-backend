package com.expensia.backend.service.report;

import com.expensia.backend.dto.response.AIInsightsResponse;
import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.GoalRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.repository.WalletRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.util.SecurityUtil;
import com.lowagie.text.PageSize;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final GoalRepository goalRepository;
    private final AIServiceClient aiServiceClient;
    private final WalletRepository walletRepository;

    public ReportService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            GoalRepository goalRepository,
            AIServiceClient aiServiceClient,
            WalletRepository walletRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.goalRepository = goalRepository;
        this.aiServiceClient = aiServiceClient;
        this.walletRepository = walletRepository;
    }

    public ReportResponse generateMonthlyReport() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
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

    public AIRecommendationResponse getRecommendations() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return getRecommendationsForUser(currentUser);
    }

    public AIRecommendationResponse getRecommendationsForUser(User user) {

        LocalDateTime start =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses =
                expenseRepository.findByUserIdAndDateBetween(
                        user.getUserId(),
                        start,
                        end
                );

        List<Income> incomes =
                incomeRepository.findByUserIdAndDateBetween(
                        user.getUserId(),
                        start,
                        end
                );

        List<SavingGoal> goals =
                goalRepository.findByUserId(user.getUserId());

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());

            String category = expense.getCategoryName() != null
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

        List<Map<String, Object>> goalData = goals.stream()
                .map(goal -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", goal.getName());
                    map.put("target_amount", goal.getTargetAmount());
                    map.put("current_amount", goal.getCurrentAmount());

                    if (goal.getDeadline() != null) {
                        map.put("deadline", goal.getDeadline().toString());
                    }

                    return map;
                })
                .toList();

        BigDecimal currentSavings = walletRepository
                .findByUserId(user.getUserId())
                .map(Wallet::getCurrentSavings)
                .orElse(BigDecimal.ZERO);

        Map<String, Object> request = new HashMap<>();

        request.put("user_id", user.getUserId());
        request.put("monthly_income", totalIncome);
        request.put("monthly_expenses", totalExpenses);
        request.put("current_savings", currentSavings);
        request.put("risk_preference", user.getRiskPreference() == null ? "MEDIUM" : user.getRiskPreference().name());
        request.put("expense_breakdown", categoryBreakdown);
        request.put("goals", goalData);

        AIRecommendationResponse response =
                aiServiceClient.getRecommendations(request);

        if (response == null || !response.isSuccess()) {
            throw new AIServiceException("Could not generate recommendations");
        }

        return response;
    }

    public AIInsightsResponse getCompleteInsights() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        LocalDateTime start =
                LocalDateTime.now()
                        .minusMonths(6)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = LocalDateTime.now();

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

        List<SavingGoal> goals =
                goalRepository.findByUserId(currentUser.getUserId());

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        List<Map<String, Object>> transactions = new ArrayList<>();

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

            Map<String, Object> transaction = new HashMap<>();

            transaction.put("date", expense.getDate().toString());
            transaction.put("category", category);
            transaction.put("amount", expense.getAmount());

            transactions.add(transaction);
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
        }

        List<Map<String, Object>> goalData = goals.stream()
                .map(goal -> {

                    Map<String, Object> map = new HashMap<>();

                    map.put("name", goal.getName());
                    map.put("target_amount", goal.getTargetAmount());
                    map.put("current_amount", goal.getCurrentAmount());

                    if (goal.getDeadline() != null) {
                        map.put("deadline", goal.getDeadline().toString());
                    }

                    return map;
                })
                .toList();

        Map<String, Object> request = new HashMap<>();

        request.put("user_id", currentUser.getUserId());

        request.put("monthly_income", totalIncome);

        request.put("monthly_expenses", totalExpenses);

        BigDecimal currentSavings = walletRepository
                .findByUserId(currentUser.getUserId())
                .map(Wallet::getCurrentSavings)
                .orElse(BigDecimal.ZERO);

        request.put("current_savings", currentSavings);

        request.put("risk_preference", currentUser.getRiskPreference() == null ? "MEDIUM" : currentUser.getRiskPreference().name());
        request.put("expense_breakdown", categoryBreakdown);

        request.put("transactions", transactions);

        request.put("goals", goalData);

        AIInsightsResponse response =
                aiServiceClient.getCompleteInsights(request);

        if (response == null || !response.isSuccess()) {
            throw new AIServiceException("Could not generate insights");
        }

        return response;
    }

    public String exportCsv(LocalDate startDate, LocalDate endDate) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        List<Expense> expenses;
        List<Income> incomes;

        if (startDate != null && endDate != null) {
            expenses = expenseRepository.findByUserIdAndDateBetween(
                    currentUser.getUserId(),
                    startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay()
            );

            incomes = incomeRepository.findByUserIdAndDateBetween(
                    currentUser.getUserId(),
                    startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay()
            );
        } else {
            expenses = expenseRepository.findByUserIdOrderByDateDesc(currentUser.getUserId());
            incomes = incomeRepository.findByUserIdOrderByDateDesc(currentUser.getUserId());
        }

        List<SavingGoal> goals = goalRepository.findByUserId(currentUser.getUserId());

        BigDecimal currentSavings = walletRepository
                .findByUserId(currentUser.getUserId())
                .map(Wallet::getCurrentSavings)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());

            String category = expense.getCategoryName() == null
                    ? "Other"
                    : expense.getCategoryName();

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

        StringBuilder csv = new StringBuilder();

        csv.append("EXPENSIA FINANCIAL REPORT\n");
        csv.append("Generated At,").append(LocalDateTime.now()).append("\n");
        csv.append("Report Period,")
                .append(startDate == null || endDate == null
                        ? "All Time"
                        : startDate + " to " + endDate)
                .append("\n\n");

        csv.append("USER INFORMATION\n");
        csv.append("Name,").append(escapeCsv(currentUser.getName())).append("\n");
        csv.append("Email,").append(escapeCsv(currentUser.getEmail())).append("\n");
        csv.append("Risk Preference,")
                .append(currentUser.getRiskPreference() == null ? "MEDIUM" : currentUser.getRiskPreference().name())
                .append("\n\n");

        csv.append("FINANCIAL SUMMARY\n");
        csv.append("Total Income,").append(totalIncome).append("\n");
        csv.append("Total Expenses,").append(totalExpenses).append("\n");
        csv.append("Balance,").append(balance).append("\n");
        csv.append("Current Savings,").append(currentSavings).append("\n\n");

        csv.append("CATEGORY BREAKDOWN\n");
        csv.append("Category,Amount\n");
        for (Map.Entry<String, BigDecimal> entry : categoryBreakdown.entrySet()) {
            csv.append(escapeCsv(entry.getKey())).append(",");
            csv.append(entry.getValue()).append("\n");
        }
        csv.append("\n");

        csv.append("EXPENSES\n");
        csv.append("Expense ID,Date,Category,Description,Merchant,Payment Method,Amount\n");
        for (Expense expense : expenses) {
            csv.append(expense.getExpenseId()).append(",");
            csv.append(expense.getDate()).append(",");
            csv.append(escapeCsv(expense.getCategoryName())).append(",");
            csv.append(escapeCsv(expense.getDescription())).append(",");
            csv.append(escapeCsv(expense.getMerchant())).append(",");
            csv.append(escapeCsv(expense.getPaymentMethod())).append(",");
            csv.append(expense.getAmount()).append("\n");
        }
        csv.append("\n");

        csv.append("INCOME\n");
        csv.append("Income ID,Date,Source,Frequency,Recurring,Amount\n");
        for (Income income : incomes) {
            csv.append(income.getIncomeId()).append(",");
            csv.append(income.getDate()).append(",");
            csv.append(escapeCsv(income.getSource())).append(",");
            csv.append(escapeCsv(income.getFrequency())).append(",");
            csv.append(income.getIsRecurring()).append(",");
            csv.append(income.getAmount()).append("\n");
        }
        csv.append("\n");

        csv.append("SAVING GOALS\n");
        csv.append("Goal ID,Name,Target Amount,Current Amount,Deadline,Status\n");
        for (SavingGoal goal : goals) {
            csv.append(goal.getGoalId()).append(",");
            csv.append(escapeCsv(goal.getName())).append(",");
            csv.append(goal.getTargetAmount()).append(",");
            csv.append(goal.getCurrentAmount()).append(",");
            csv.append(goal.getDeadline()).append(",");
            csv.append(goal.getStatus()).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    public byte[] exportPdf(LocalDate startDate, LocalDate endDate) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);

            document.open();

            document.add(new Paragraph("EXPENSIA FINANCIAL REPORT"));
            document.add(new Paragraph("Generated At: " + LocalDateTime.now()));
            document.add(new Paragraph(
                    "Report Period: " +
                            (startDate == null || endDate == null
                                    ? "All Time"
                                    : startDate + " to " + endDate)
            ));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("USER INFORMATION"));
            document.add(new Paragraph("Name: " + currentUser.getName()));
            document.add(new Paragraph("Email: " + currentUser.getEmail()));
            document.add(new Paragraph(
                    "Risk Preference: " +
                            (currentUser.getRiskPreference() == null
                                    ? "MEDIUM"
                                    : currentUser.getRiskPreference().name())
            ));
            document.add(new Paragraph(" "));

            List<Expense> expenses;
            List<Income> incomes;

            if (startDate != null && endDate != null) {
                expenses = expenseRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );

                incomes = incomeRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );
            } else {
                expenses = expenseRepository.findByUserIdOrderByDateDesc(currentUser.getUserId());
                incomes = incomeRepository.findByUserIdOrderByDateDesc(currentUser.getUserId());
            }

            List<SavingGoal> goals = goalRepository.findByUserId(currentUser.getUserId());

            BigDecimal currentSavings = walletRepository
                    .findByUserId(currentUser.getUserId())
                    .map(Wallet::getCurrentSavings)
                    .orElse(BigDecimal.ZERO);

            BigDecimal totalExpense = BigDecimal.ZERO;
            BigDecimal totalIncome = BigDecimal.ZERO;

            Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

            for (Expense e : expenses) {
                totalExpense = totalExpense.add(e.getAmount());

                String category = e.getCategoryName() == null ? "Other" : e.getCategoryName();

                categoryBreakdown.put(
                        category,
                        categoryBreakdown.getOrDefault(category, BigDecimal.ZERO)
                                .add(e.getAmount())
                );
            }

            for (Income i : incomes) {
                totalIncome = totalIncome.add(i.getAmount());
            }

            document.add(new Paragraph("FINANCIAL SUMMARY"));
            document.add(new Paragraph("Total Income: " + totalIncome));
            document.add(new Paragraph("Total Expenses: " + totalExpense));
            document.add(new Paragraph("Balance: " + totalIncome.subtract(totalExpense)));
            document.add(new Paragraph("Current Savings: " + currentSavings));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("CATEGORY BREAKDOWN"));

            PdfPTable categoryTable = new PdfPTable(2);
            categoryTable.setWidthPercentage(60);
            categoryTable.addCell("Category");
            categoryTable.addCell("Amount");

            for (Map.Entry<String, BigDecimal> entry : categoryBreakdown.entrySet()) {
                categoryTable.addCell(entry.getKey());
                categoryTable.addCell(entry.getValue().toString());
            }

            document.add(categoryTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("EXPENSES"));

            PdfPTable expenseTable = new PdfPTable(5);
            expenseTable.setWidthPercentage(100);
            expenseTable.addCell("Date");
            expenseTable.addCell("Category");
            expenseTable.addCell("Merchant");
            expenseTable.addCell("Payment");
            expenseTable.addCell("Amount");

            for (Expense e : expenses) {
                expenseTable.addCell(e.getDate() == null ? "" : e.getDate().toLocalDate().toString());
                expenseTable.addCell(e.getCategoryName() == null ? "Other" : e.getCategoryName());
                expenseTable.addCell(e.getMerchant() == null ? "" : e.getMerchant());
                expenseTable.addCell(e.getPaymentMethod() == null ? "" : e.getPaymentMethod());
                expenseTable.addCell(e.getAmount().toString());
            }

            document.add(expenseTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("INCOME"));

            PdfPTable incomeTable = new PdfPTable(5);
            incomeTable.setWidthPercentage(100);
            incomeTable.addCell("Date");
            incomeTable.addCell("Source");
            incomeTable.addCell("Frequency");
            incomeTable.addCell("Recurring");
            incomeTable.addCell("Amount");

            for (Income i : incomes) {
                incomeTable.addCell(i.getDate() == null ? "" : i.getDate().toLocalDate().toString());
                incomeTable.addCell(i.getSource() == null ? "" : i.getSource());
                incomeTable.addCell(i.getFrequency() == null ? "" : i.getFrequency());
                incomeTable.addCell(i.getIsRecurring() == null ? "" : i.getIsRecurring().toString());
                incomeTable.addCell(i.getAmount().toString());
            }

            document.add(incomeTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("SAVING GOALS"));

            PdfPTable goalTable = new PdfPTable(4);
            goalTable.setWidthPercentage(100);
            goalTable.addCell("Goal");
            goalTable.addCell("Target");
            goalTable.addCell("Current");
            goalTable.addCell("Deadline");

            for (SavingGoal g : goals) {
                goalTable.addCell(g.getName() == null ? "" : g.getName());
                goalTable.addCell(g.getTargetAmount().toString());
                goalTable.addCell(g.getCurrentAmount().toString());
                goalTable.addCell(g.getDeadline() == null ? "" : g.getDeadline().toString());
            }

            document.add(goalTable);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Could not generate PDF report");
        }
    }

    public Object getForecast() {
        AIInsightsResponse insights = getCompleteInsights();

        if (insights == null || !insights.isSuccess()) {
            throw new AIServiceException("Could not generate forecast");
        }

        return insights.getForecast();
    }

    public Object getBenchmarks() {
        Object response = aiServiceClient.getBenchmarks();

        if (response == null) {
            throw new AIServiceException("Could not retrieve benchmarks");
        }

        return response;
    }
}