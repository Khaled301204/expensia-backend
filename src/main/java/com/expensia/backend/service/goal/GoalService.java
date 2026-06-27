package com.expensia.backend.service.goal;

import com.expensia.backend.dto.request.GoalRequest;
import com.expensia.backend.dto.response.GoalResponse;
import com.expensia.backend.dto.response.WalletResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.repository.GoalRepository;
import com.expensia.backend.service.wallet.WalletService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final WalletService walletService;

    public GoalService(
            GoalRepository goalRepository,
            WalletService walletService
    ) {
        this.goalRepository = goalRepository;
        this.walletService = walletService;
    }

    public GoalResponse createGoal(GoalRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        BigDecimal initialAmount =
                request.getCurrentAmount() == null
                        ? BigDecimal.ZERO
                        : request.getCurrentAmount();

        if (initialAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current amount cannot be negative");
        }

        if (initialAmount.compareTo(request.getTargetAmount()) > 0) {
            throw new IllegalArgumentException("Current amount cannot exceed target amount");
        }

        if (initialAmount.compareTo(BigDecimal.ZERO) > 0) {
            WalletResponse wallet = walletService.getWallet();

            if (wallet.getCurrentSavings().compareTo(initialAmount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient available savings to initialize this goal."
                );
            }

            walletService.decreaseSavings(
                    currentUser.getUserId(),
                    initialAmount
            );
        }

        SavingGoal goal = new SavingGoal();
        goal.setUserId(currentUser.getUserId());
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(initialAmount);
        goal.setDeadline(request.getDeadline());

        if (initialAmount.compareTo(request.getTargetAmount()) == 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else {
            goal.setStatus(GoalStatus.ACTIVE);
        }

        SavingGoal savedGoal = goalRepository.save(goal);

        return mapToResponse(savedGoal);
    }

    public List<GoalResponse> getMyGoals() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return goalRepository.findByUserId(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GoalResponse addSavings(Long goalId, BigDecimal amount) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero");
        }

        SavingGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (goal.getStatus() == GoalStatus.COMPLETED) {
            throw new IllegalArgumentException("Goal is already completed");
        }

        WalletResponse wallet = walletService.getWallet();

        if (wallet.getCurrentSavings().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient available savings to contribute to this goal."
            );
        }

        walletService.decreaseSavings(
                currentUser.getUserId(),
                amount
        );

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatus.COMPLETED);
        }

        SavingGoal savedGoal = goalRepository.save(goal);

        return mapToResponse(savedGoal);
    }

    public void deleteGoal(Long goalId) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        SavingGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        goalRepository.delete(goal);
    }

    private GoalResponse mapToResponse(SavingGoal goal) {
        return new GoalResponse(
                goal.getGoalId(),
                goal.getUserId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getStatus(),
                goal.getCreatedAt()
        );
    }
}