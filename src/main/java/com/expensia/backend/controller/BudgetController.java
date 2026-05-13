package com.expensia.backend.controller;

import com.expensia.backend.dto.request.BudgetRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.BudgetResponse;
import com.expensia.backend.service.budget.BudgetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ApiResponse<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        return ApiResponse.success(
                "Budget created successfully",
                budgetService.createBudget(request)
        );
    }

    @GetMapping
    public ApiResponse<List<BudgetResponse>> getMyBudgets() {
        return ApiResponse.success(
                "Budgets retrieved successfully",
                budgetService.getMyBudgets()
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ApiResponse.success("Budget deleted successfully", null);
    }
}