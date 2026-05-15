package com.expensia.backend.dto.response;

import com.expensia.backend.model.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalResponse {

    private Long goalId;
    private Long userId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private GoalStatus status;
    private LocalDateTime createdAt;

    public GoalResponse(
            Long goalId,
            Long userId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            GoalStatus status,
            LocalDateTime createdAt
    ) {
        this.goalId = goalId;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getGoalId() {
        return goalId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}