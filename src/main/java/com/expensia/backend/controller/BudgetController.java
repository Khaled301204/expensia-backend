package com.expensia.backend.controller;

import com.expensia.backend.dto.request.BudgetRequest;
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
    public BudgetResponse createBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.createBudget(request);
    }

    @GetMapping
    public List<BudgetResponse> getMyBudgets() {
        return budgetService.getMyBudgets();
    }

    @DeleteMapping("/{id}")
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
    }
}