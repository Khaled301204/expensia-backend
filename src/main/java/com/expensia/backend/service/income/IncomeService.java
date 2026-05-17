package com.expensia.backend.service.income;

import com.expensia.backend.dto.request.IncomeRequest;
import com.expensia.backend.dto.response.IncomeResponse;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public IncomeResponse createIncome(IncomeRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Income income = new Income();
        income.setUserId(currentUser.getUserId());
        income.setAmount(request.getAmount());
        income.setDate(request.getDate());
        income.setSource(request.getSource());
        income.setFrequency(request.getFrequency());
        income.setIsRecurring(request.getIsRecurring());

        Income savedIncome = incomeRepository.save(income);
        return mapToResponse(savedIncome);
    }

    public List<IncomeResponse> getMyIncomes() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return incomeRepository.findByUserIdOrderByDateDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteIncome(Long incomeId) {
        incomeRepository.deleteById(incomeId);
    }

    private IncomeResponse mapToResponse(Income income) {
        return new IncomeResponse(
                income.getIncomeId(),
                income.getUserId(),
                income.getAmount(),
                income.getDate(),
                income.getSource(),
                income.getFrequency(),
                income.getIsRecurring()
        );
    }
}