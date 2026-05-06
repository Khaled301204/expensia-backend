package com.expensia.backend.service.goal;

import com.expensia.backend.dto.request.GoalRequest;
import com.expensia.backend.dto.response.GoalResponse;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.repository.GoalRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public GoalResponse createGoal(GoalRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        SavingGoal goal = new SavingGoal();
        goal.setUserId(currentUser.getUserId());
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(
                request.getCurrentAmount() == null ? BigDecimal.ZERO : request.getCurrentAmount()
        );
        goal.setDeadline(request.getDeadline());
        goal.setStatus(GoalStatus.ACTIVE);

        SavingGoal savedGoal = goalRepository.save(goal);

        return mapToResponse(savedGoal);
    }

    public List<GoalResponse> getMyGoals() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        return goalRepository.findByUserId(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GoalResponse addSavings(Long goalId, BigDecimal amount) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        SavingGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        SavingGoal savedGoal = goalRepository.save(goal);

        return mapToResponse(savedGoal);
    }

    public void deleteGoal(Long goalId) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        SavingGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }

        goalRepository.delete(goal);
    }

    private GoalResponse mapToResponse(SavingGoal goal) {
        return new GoalResponse(
                goal.getGoalId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getStatus()
        );
    }
}