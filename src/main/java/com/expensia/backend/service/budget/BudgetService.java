package com.expensia.backend.service.budget;

import com.expensia.backend.dto.request.BudgetRequest;
import com.expensia.backend.dto.response.BudgetResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;


    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public BudgetResponse createBudget(BudgetRequest request) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Budget budget = budgetRepository
                .findFirstByUserIdAndCategoryId(
                        currentUser.getUserId(),
                        request.getCategoryId()
                )
                .orElse(new Budget());

        budget.setUserId(currentUser.getUserId());
        budget.setCategoryId(request.getCategoryId());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(
                request.getAlertThreshold() == null
                        ? 0.8
                        : request.getAlertThreshold()
        );

        Budget savedBudget = budgetRepository.save(budget);

        return mapToResponse(savedBudget);
    }

    public List<BudgetResponse> getMyBudgets() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return budgetRepository.findByUserId(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteBudget(Long budgetId) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Budget not found"));

        if (!budget.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        budgetRepository.delete(budget);
    }



    private BudgetResponse mapToResponse(Budget budget) {

        BigDecimal spentAmount = expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(
                budget.getUserId(),
                budget.getCategoryId(),
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().plusDays(1).atStartOfDay()
        );

        if (spentAmount == null) {
            spentAmount = BigDecimal.ZERO;
        }

        Boolean isOverBudget =
                spentAmount.compareTo(budget.getLimitAmount()) > 0;

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getUserId(),
                budget.getCategoryId(),
                budget.getLimitAmount(),
                spentAmount,
                budget.getAlertThreshold(),
                isOverBudget,
                budget.getStartDate(),
                budget.getEndDate()
        );
    }
}