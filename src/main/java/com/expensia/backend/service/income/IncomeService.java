package com.expensia.backend.service.income;

import com.expensia.backend.dto.request.IncomeRequest;
import com.expensia.backend.dto.request.UpdateIncomeRequest;
import com.expensia.backend.dto.response.IncomeResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.service.wallet.WalletService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final WalletService walletService;

    public IncomeService(IncomeRepository incomeRepository, WalletService walletService) {
        this.incomeRepository = incomeRepository;
        this.walletService = walletService;
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

        walletService.increaseSavings(
                currentUser.getUserId(),
                savedIncome.getAmount()
        );
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

    public IncomeResponse getIncomeById(Long incomeId) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        if (!income.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return mapToResponse(income);
    }

    public IncomeResponse updateIncome(Long incomeId, UpdateIncomeRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        if (!income.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        BigDecimal oldAmount = income.getAmount();

        if (request.getAmount() != null) {
            income.setAmount(request.getAmount());
        }

        if (request.getDate() != null) {
            income.setDate(request.getDate());
        }

        if (request.getSource() != null) {
            income.setSource(request.getSource());
        }

        if (request.getFrequency() != null) {
            income.setFrequency(request.getFrequency());
        }

        if (request.getIsRecurring() != null) {
            income.setIsRecurring(request.getIsRecurring());
        }

        Income savedIncome = incomeRepository.save(income);

        BigDecimal newAmount = savedIncome.getAmount();
        BigDecimal difference = newAmount.subtract(oldAmount);

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            walletService.increaseSavings(currentUser.getUserId(), difference);
        } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            walletService.decreaseSavings(currentUser.getUserId(), difference.abs());
        }

        return mapToResponse(savedIncome);
    }

    public void deleteIncome(Long incomeId) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        if (!income.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        walletService.decreaseSavings(
                currentUser.getUserId(),
                income.getAmount()
        );

        incomeRepository.delete(income);
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