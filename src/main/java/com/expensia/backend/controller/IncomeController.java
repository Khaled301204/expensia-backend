package com.expensia.backend.controller;

import com.expensia.backend.dto.request.IncomeRequest;
import com.expensia.backend.dto.request.UpdateIncomeRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.IncomeResponse;
import com.expensia.backend.service.income.IncomeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ApiResponse<IncomeResponse> createIncome(@Valid @RequestBody IncomeRequest request) {
        return ApiResponse.success(
                "Income created successfully",
                incomeService.createIncome(request)
        );
    }

    @GetMapping
    public ApiResponse<List<IncomeResponse>> getMyIncomes() {
        return ApiResponse.success(
                "Incomes retrieved successfully",
                incomeService.getMyIncomes()
        );
    }

    @GetMapping("/{incomeId}")
    public ApiResponse<IncomeResponse> getIncomeById(@PathVariable Long incomeId) {
        return ApiResponse.success(
                "Income retrieved successfully",
                incomeService.getIncomeById(incomeId)
        );
    }

    @PutMapping("/{incomeId}")
    public ApiResponse<IncomeResponse> updateIncome(
            @PathVariable Long incomeId,
            @RequestBody UpdateIncomeRequest request
    ) {
        return ApiResponse.success(
                "Income updated successfully",
                incomeService.updateIncome(incomeId, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ApiResponse.success("Income deleted successfully", null);
    }
}