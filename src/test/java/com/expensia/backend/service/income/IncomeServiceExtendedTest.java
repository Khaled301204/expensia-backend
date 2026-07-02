package com.expensia.backend.service.income;

import com.expensia.backend.dto.request.UpdateIncomeRequest;
import com.expensia.backend.dto.response.IncomeResponse;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.service.wallet.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceExtendedTest {

    @Mock private IncomeRepository incomeRepository;
    @Mock private WalletService walletService;

    @InjectMocks
    private IncomeService incomeService;

    @BeforeEach
    void setUp() {
        User user = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Income income(Long id, BigDecimal amount) {
        Income i = new Income();
        i.setIncomeId(id);
        i.setUserId(1L);
        i.setAmount(amount);
        i.setSource("Salary");
        i.setDate(LocalDateTime.now());
        i.setIsRecurring(false);
        i.setRecurringActive(false);
        return i;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getMyIncomes_returnsAllForUser() {
        when(incomeRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(income(1L, new BigDecimal("3000")), income(2L, new BigDecimal("500"))));

        List<IncomeResponse> result = incomeService.getMyIncomes();

        assertEquals(2, result.size());
    }

    @Test
    void updateIncome_amountIncrease_increasesWalletByDiff() {
        Income existing = income(1L, new BigDecimal("2000"));
        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "amount", new BigDecimal("2500"));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        incomeService.updateIncome(1L, req);

        verify(walletService).increaseSavings(1L, new BigDecimal("500"));
    }

    @Test
    void updateIncome_amountDecrease_decreasesWalletByDiff() {
        Income existing = income(1L, new BigDecimal("3000"));
        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "amount", new BigDecimal("2000"));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        incomeService.updateIncome(1L, req);

        verify(walletService).decreaseSavings(1L, new BigDecimal("1000"));
    }

    @Test
    void updateIncome_sameAmount_noWalletChange() {
        Income existing = income(1L, new BigDecimal("3000"));
        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "amount", new BigDecimal("3000"));
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        incomeService.updateIncome(1L, req);

        verify(walletService, never()).increaseSavings(any(), any());
        verify(walletService, never()).decreaseSavings(any(), any());
    }

    @Test
    void updateIncome_sourceChange_updatesSource() {
        Income existing = income(1L, new BigDecimal("3000"));
        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "source", "Freelance");
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IncomeResponse response = incomeService.updateIncome(1L, req);

        assertEquals("Freelance", response.getSource());
    }

    @Test
    void updateIncome_toggleRecurringOn_setsNextOccurrence() {
        LocalDateTime date = LocalDateTime.of(2026, java.time.Month.JULY, 1, 0, 0);
        Income existing = income(1L, new BigDecimal("3000"));
        existing.setDate(date);
        existing.setIsRecurring(false);

        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "isRecurring", true);
        setField(req, "frequency", "MONTHLY");
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IncomeResponse response = incomeService.updateIncome(1L, req);

        assertTrue(response.getIsRecurring());
        assertEquals(date.plusMonths(1), response.getNextOccurrence());
    }

    @Test
    void updateIncome_pauseRecurring_setsActiveFalse() {
        Income existing = income(1L, new BigDecimal("3000"));
        existing.setIsRecurring(true);
        existing.setFrequency("MONTHLY");
        existing.setRecurringActive(true);
        existing.setNextOccurrence(LocalDateTime.now().plusMonths(1));

        UpdateIncomeRequest req = new UpdateIncomeRequest();
        setField(req, "recurringActive", false);
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IncomeResponse response = incomeService.updateIncome(1L, req);

        assertFalse(response.getRecurringActive());
    }
}
