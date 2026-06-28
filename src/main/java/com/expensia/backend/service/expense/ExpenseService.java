package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.request.UpdateExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.dto.response.NLPParseResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.service.budget.BudgetAlertService;
import com.expensia.backend.service.wallet.WalletService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AIServiceClient aiServiceClient;
    private final BudgetAlertService budgetAlertService;
    private final CategoryRepository categoryRepository;
    private final WalletService walletService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            AIServiceClient aiServiceClient,
            CategoryRepository categoryRepository,
            WalletService walletService,
            BudgetAlertService budgetAlertService
    ) {
        this.expenseRepository = expenseRepository;
        this.aiServiceClient = aiServiceClient;
        this.budgetAlertService = budgetAlertService;
        this.categoryRepository = categoryRepository;
        this.walletService = walletService;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = new Expense();
        expense.setUserId(currentUser.getUserId());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setFrequency(request.getFrequency());
        expense.setCreatedByVoice(false);
        expense.setDate(request.getDate());

        if (Boolean.TRUE.equals(request.getIsRecurring())) {
            expense.setRecurringActive(
                    request.getRecurringActive() == null || request.getRecurringActive()
            );

            expense.setNextOccurrence(
                    calculateNextOccurrence(
                            expense.getDate() == null ? LocalDateTime.now() : expense.getDate(),
                            expense.getFrequency()
                    )
            );
        } else {
            expense.setRecurringActive(false);
            expense.setNextOccurrence(null);
        }

        AICategorizationResponse aiResponse =
                aiServiceClient.categorizeExpense(
                        request.getDescription(),
                        request.getMerchant()
                );

        String categoryName = aiResponse.getCategory();
        Double confidence = aiResponse.getConfidence();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        expense.setCategoryName(categoryName);
        expense.setCategoryConfidence(confidence == null ? 0.0 : confidence);

        Category category = categoryRepository
                .findByNameIgnoreCase(categoryName)
                .orElse(null);

        if (category != null) {
            expense.setCategoryId(category.getCategoryId());
        }

        validateRecurringExpense(expense);
        Expense savedExpense = expenseRepository.save(expense);

        budgetAlertService.checkBudgetAlert(currentUser.getUserId(), savedExpense);

        walletService.decreaseSavings(
                currentUser.getUserId(),
                savedExpense.getAmount()
        );

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

    public void deleteExpense(Long expenseId) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        walletService.increaseSavings(
                currentUser.getUserId(),
                expense.getAmount()
        );

        expenseRepository.delete(expense);
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

        expense.setCategoryConfidence(confidence == null ? 0.0 : confidence);

        if (parsed.getDate() != null) {
            expense.setDate(LocalDate.parse(parsed.getDate()).atStartOfDay());
        }

        expense.setPaymentMethod("CASH");
        expense.setIsRecurring(false);
        expense.setRecurringActive(false);
        expense.setFrequency(null);
        expense.setNextOccurrence(null);
        expense.setCreatedByVoice(true);

        Expense savedExpense = expenseRepository.save(expense);

        budgetAlertService.checkBudgetAlert(currentUser.getUserId(), savedExpense);

        walletService.decreaseSavings(
                currentUser.getUserId(),
                savedExpense.getAmount()
        );

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
        BigDecimal oldAmount = expense.getAmount();
        boolean recurringScheduleChanged = false;
        Boolean oldIsRecurring = expense.getIsRecurring();

        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }

        if (request.getDate() != null) {
            expense.setDate(request.getDate());
            recurringScheduleChanged = true;
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

            if (!request.getIsRecurring().equals(oldIsRecurring)) {
                recurringScheduleChanged = true;
            }
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
            expense.setCategoryConfidence(confidence == null ? 0.0 : confidence);

            Category category = categoryRepository
                    .findByNameIgnoreCase(categoryName)
                    .orElse(null);

            if (category != null) {
                expense.setCategoryId(category.getCategoryId());
            }
        }

        if (request.getFrequency() != null) {
            expense.setFrequency(request.getFrequency());
            recurringScheduleChanged = true;
        }

        if (request.getRecurringActive() != null) {
            expense.setRecurringActive(request.getRecurringActive());
        }

        if (Boolean.TRUE.equals(expense.getIsRecurring())) {
            expense.setRecurringActive(
                    expense.getRecurringActive() == null
                            ? true
                            : expense.getRecurringActive()
            );

            if (recurringScheduleChanged || expense.getNextOccurrence() == null) {
                expense.setNextOccurrence(
                        calculateNextOccurrence(
                                expense.getDate(),
                                expense.getFrequency()
                        )
                );
            }
        } else {
            expense.setRecurringActive(false);
            expense.setNextOccurrence(null);
        }

        validateRecurringExpense(expense);
        Expense savedExpense = expenseRepository.save(expense);

        BigDecimal newAmount = savedExpense.getAmount();
        BigDecimal difference = newAmount.subtract(oldAmount);

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            walletService.decreaseSavings(currentUser.getUserId(), difference);
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            walletService.increaseSavings(currentUser.getUserId(), difference.abs());
        }

        budgetAlertService.checkBudgetAlert(currentUser.getUserId(), savedExpense);

        return mapToResponse(savedExpense);
    }

    private LocalDateTime calculateNextOccurrence(LocalDateTime date, String frequency) {
        if (date == null || frequency == null) {
            return null;
        }

        return switch (frequency.toUpperCase()) {
            case "DAILY" -> date.plusDays(1);
            case "WEEKLY" -> date.plusWeeks(1);
            case "MONTHLY" -> date.plusMonths(1);
            case "YEARLY" -> date.plusYears(1);
            default -> null;
        };
    }

    private void validateRecurringExpense(Expense expense) {
        if (Boolean.TRUE.equals(expense.getIsRecurring())) {
            if (expense.getFrequency() == null || expense.getFrequency().isBlank()) {
                throw new IllegalArgumentException("Frequency is required for recurring expenses");
            }

            String frequency = expense.getFrequency().toUpperCase();

            if (!frequency.equals("DAILY")
                    && !frequency.equals("WEEKLY")
                    && !frequency.equals("MONTHLY")
                    && !frequency.equals("YEARLY")) {
                throw new IllegalArgumentException("Invalid expense frequency");
            }
        }
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
                expense.getFrequency(),
                expense.getNextOccurrence(),
                expense.getRecurringActive(),
                expense.getCreatedByVoice(),
                expense.getCreatedAt()
        );
    }
}