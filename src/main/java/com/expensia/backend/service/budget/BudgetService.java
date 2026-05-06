package com.expensia.backend.service.budget;

import com.expensia.backend.dto.request.BudgetRequest;
import com.expensia.backend.dto.response.BudgetResponse;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public BudgetResponse createBudget(BudgetRequest request) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        Budget budget = new Budget();

        budget.setUserId(currentUser.getUserId());
        budget.setCategoryId(request.getCategoryId());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());

        Budget savedBudget = budgetRepository.save(budget);

        return mapToResponse(savedBudget);
    }

    public List<BudgetResponse> getMyBudgets() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        return budgetRepository.findByUserId(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteBudget(Long budgetId) {
        budgetRepository.deleteById(budgetId);
    }

    private BudgetResponse mapToResponse(Budget budget) {

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getCategoryId(),
                budget.getLimitAmount(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }
}