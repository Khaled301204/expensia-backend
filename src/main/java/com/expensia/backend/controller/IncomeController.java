package com.expensia.backend.controller;

import com.expensia.backend.dto.request.IncomeRequest;
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
    public IncomeResponse createIncome(@Valid @RequestBody IncomeRequest request) {
        return incomeService.createIncome(request);
    }

    @GetMapping
    public List<IncomeResponse> getMyIncomes() {
        return incomeService.getMyIncomes();
    }

    @DeleteMapping("/{id}")
    public void deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
    }
}