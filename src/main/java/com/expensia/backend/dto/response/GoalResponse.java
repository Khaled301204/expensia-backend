package com.expensia.backend.dto.response;

import com.expensia.backend.model.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalResponse {

    private Long goalId;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private GoalStatus status;

    public GoalResponse(
            Long goalId,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate deadline,
            GoalStatus status
    ) {
        this.goalId = goalId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.status = status;
    }

    public Long getGoalId() {
        return goalId;
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
}