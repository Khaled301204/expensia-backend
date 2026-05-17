package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.request.UpdateExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.dto.response.NLPParseResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.service.notification.NotificationService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AIServiceClient aiServiceClient;
    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final CategoryRepository categoryRepository;

    public ExpenseService( ExpenseRepository expenseRepository, AIServiceClient aiServiceClient, BudgetRepository budgetRepository, NotificationService notificationService, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.aiServiceClient = aiServiceClient;
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
        this.categoryRepository = categoryRepository;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = new Expense();
        expense.setUserId(currentUser.getUserId());
        expense.setAmount(request.getAmount());
        expense.setCategoryId(request.getCategoryId());
        expense.setDescription(request.getDescription());
        AICategorizationResponse aiResponse = aiServiceClient.categorizeExpense( request.getDescription(), request.getMerchant());
        String categoryName = aiResponse.getCategory();
        Category category = categoryRepository
                .findByNameIgnoreCase(categoryName)
                .orElse(null);

        if (category != null) {
            expense.setCategoryId(category.getCategoryId());
        }
        Double confidence = aiResponse.getConfidence();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        expense.setCategoryName(categoryName);

        expense.setCategoryConfidence(
                confidence == null ? 0.0 : confidence
        );
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setCreatedByVoice(false);

        Expense savedExpense = expenseRepository.save(expense);
        checkBudgetAlert(currentUser.getUserId(), savedExpense);
        return mapToResponse(savedExpense);
    }

    public List<ExpenseResponse> getMyExpenses() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return expenseRepository.findByUserIdOrderByDateDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void checkBudgetAlert(Long userId, Expense expense) {

        if (expense.getCategoryId() == null) {
            return;
        }

        List<Budget> budgets = budgetRepository.findByUserIdAndCategoryId(
                userId,
                expense.getCategoryId()
        );

        for (Budget budget : budgets) {

            BigDecimal totalSpent = expenseRepository.sumByUserIdAndDateBetween(
                    userId,
                    budget.getStartDate().atStartOfDay(),
                    budget.getEndDate().plusDays(1).atStartOfDay()
            );

            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            if (totalSpent.compareTo(budget.getLimitAmount()) > 0) {
                notificationService.createNotification(
                        userId,
                        "Budget exceeded",
                        "You exceeded your budget limit for category ID " + budget.getCategoryId(),
                        NotificationType.BUDGET_EXCEEDED
                );
            }
        }
    }

    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getExpenseId(),
                expense.getUserId(),
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getCategoryName(),
                expense.getCategoryConfidence() == null ? 0.0 : expense.getCategoryConfidence(),
                expense.getDate(),
                expense.getDescription(),
                expense.getMerchant(),
                expense.getPaymentMethod(),
                expense.getIsRecurring(),
                expense.getCreatedByVoice(),
                expense.getCreatedAt()
        );
    }

    public ExpenseResponse parseAndCreateExpense(String text) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        NLPParseResponse aiResponse = aiServiceClient.parseExpenseText(text);

        if (aiResponse == null || !aiResponse.isSuccess() || aiResponse.getParsed() == null) {
            throw new AIServiceException("Could not parse expense text");
        }

        NLPParseResponse.ParsedExpense parsed = aiResponse.getParsed();

        Expense expense = new Expense();

        expense.setUserId(currentUser.getUserId());
        expense.setAmount(BigDecimal.valueOf(parsed.getAmount()));
        expense.setMerchant(parsed.getMerchant());
        expense.setDescription(parsed.getDescription());
        Double confidence =
                parsed.getConfidence() != null
                        ? parsed.getConfidence().get("category")
                        : 0.0;

        String categoryName = parsed.getCategory();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        expense.setCategoryName(categoryName);
        Category category = categoryRepository
                .findByNameIgnoreCase(categoryName)
                .orElse(null);

        if (category != null) {
            expense.setCategoryId(category.getCategoryId());
        }

        expense.setCategoryConfidence(
                confidence == null ? 0.0 : confidence
        );

        if (parsed.getDate() != null) {
            expense.setDate(LocalDate.parse(parsed.getDate()).atStartOfDay());
        }

        expense.setPaymentMethod("CASH");
        expense.setIsRecurring(false);
        expense.setCreatedByVoice(true);

        Expense savedExpense = expenseRepository.save(expense);

        checkBudgetAlert(currentUser.getUserId(), savedExpense);

        return mapToResponse(savedExpense);
    }

    public ExpenseResponse getExpenseById(Long expenseId) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return mapToResponse(expense);
    }

    public ExpenseResponse updateExpense(Long expenseId, UpdateExpenseRequest request) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        boolean recategorize = false;

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }

        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }

        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
            recategorize = true;
        }

        if (request.getMerchant() != null) {
            expense.setMerchant(request.getMerchant());
            recategorize = true;
        }

        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
        }

        if (request.getIsRecurring() != null) {
            expense.setIsRecurring(request.getIsRecurring());
        }

        if (recategorize) {

            AICategorizationResponse aiResponse =
                    aiServiceClient.categorizeExpense(
                            expense.getDescription(),
                            expense.getMerchant()
                    );

            String categoryName = aiResponse.getCategory();
            Double confidence = aiResponse.getConfidence();

            if (confidence == null || confidence < 0.40) {
                categoryName = "Other";
            }

            expense.setCategoryName(categoryName);
            expense.setCategoryConfidence(
                    confidence == null ? 0.0 : confidence
            );

            Category category = categoryRepository
                    .findByNameIgnoreCase(categoryName)
                    .orElse(null);

            if (category != null) {
                expense.setCategoryId(category.getCategoryId());
            }
        }

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }
}