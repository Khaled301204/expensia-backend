package com.expensia.backend.service.income;

import com.expensia.backend.model.entity.Income;
import com.expensia.backend.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public Income createIncome(Long userId, Income income) {
        income.setUserId(userId);
        return incomeRepository.save(income);
    }

    public List<Income> getIncomes(Long userId) {
        return incomeRepository.findByUserIdOrderByDateDesc(userId);
    }

    public BigDecimal getTotalIncome(Long userId, LocalDateTime start, LocalDateTime end) {
        BigDecimal total = incomeRepository.sumByUserIdAndDateBetween(userId, start, end);
        return total != null ? total : BigDecimal.ZERO;
    }
}
