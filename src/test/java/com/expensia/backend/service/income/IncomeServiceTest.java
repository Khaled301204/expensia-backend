package com.expensia.backend.service.income;

import com.expensia.backend.dto.request.IncomeRequest;
import com.expensia.backend.dto.response.IncomeResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private IncomeService incomeService;

    @BeforeEach
    void setUpSecurityContext() {
        User user = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private IncomeRequest buildRequest(BigDecimal amount, String source, Boolean isRecurring, String frequency) {
        IncomeRequest req = new IncomeRequest();
        setField(req, "amount", amount);
        setField(req, "source", source);
        setField(req, "date", LocalDateTime.now());
        setField(req, "isRecurring", isRecurring);
        setField(req, "frequency", frequency);
        return req;
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

    private Income savedIncome(Long id, BigDecimal amount, Boolean isRecurring, String frequency, LocalDateTime nextOccurrence) {
        Income i = new Income();
        i.setIncomeId(id);
        i.setUserId(1L);
        i.setAmount(amount);
        i.setSource("Salary");
        i.setDate(LocalDateTime.now());
        i.setIsRecurring(isRecurring);
        i.setFrequency(frequency);
        i.setNextOccurrence(nextOccurrence);
        i.setRecurringActive(isRecurring);
        return i;
    }

    @Test
    void createIncome_nonRecurring_savesAndIncreasesWallet() {
        IncomeRequest request = buildRequest(new BigDecimal("3000"), "Salary", false, null);
        when(incomeRepository.save(any())).thenAnswer(inv -> {
            Income i = inv.getArgument(0);
            i.setIncomeId(1L);
            return i;
        });

        IncomeResponse response = incomeService.createIncome(request);

        assertEquals(new BigDecimal("3000"), response.getAmount());
        assertFalse(response.getIsRecurring());
        assertNull(response.getNextOccurrence());
        verify(walletService).increaseSavings(1L, new BigDecimal("3000"));
    }

    @Test
    void createIncome_recurring_setsNextOccurrence() {
        LocalDateTime date = LocalDateTime.of(2026, java.time.Month.JULY, 1, 0, 0);
        IncomeRequest request = buildRequest(new BigDecimal("5000"), "Salary", true, "MONTHLY");
        setField(request, "date", date);

        when(incomeRepository.save(any())).thenAnswer(inv -> {
            Income i = inv.getArgument(0);
            i.setIncomeId(1L);
            return i;
        });

        IncomeResponse response = incomeService.createIncome(request);

        assertTrue(response.getIsRecurring());
        assertTrue(response.getRecurringActive());
        assertEquals(date.plusMonths(1), response.getNextOccurrence());
    }

    @Test
    void createIncome_recurringWithoutFrequency_throws() {
        IncomeRequest request = buildRequest(new BigDecimal("3000"), "Salary", true, null);

        assertThrows(IllegalArgumentException.class, () -> incomeService.createIncome(request));
        verify(incomeRepository, never()).save(any());
    }

    @Test
    void createIncome_recurringWithInvalidFrequency_throws() {
        IncomeRequest request = buildRequest(new BigDecimal("3000"), "Salary", true, "BIWEEKLY");

        assertThrows(IllegalArgumentException.class, () -> incomeService.createIncome(request));
    }

    @Test
    void deleteIncome_notFound_throws() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incomeService.deleteIncome(99L));
    }

    @Test
    void deleteIncome_decreasesWalletAndDeletes() {
        Income income = savedIncome(5L, new BigDecimal("2000"), false, null, null);
        when(incomeRepository.findById(5L)).thenReturn(Optional.of(income));

        incomeService.deleteIncome(5L);

        verify(walletService).decreaseSavings(1L, new BigDecimal("2000"));
        verify(incomeRepository).delete(income);
    }

    @Test
    void getIncomeById_notFound_throws() {
        when(incomeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incomeService.getIncomeById(42L));
    }

    @Test
    void getIncomeById_success_returnsResponse() {
        Income income = savedIncome(3L, new BigDecimal("1500"), false, null, null);
        when(incomeRepository.findById(3L)).thenReturn(Optional.of(income));

        IncomeResponse response = incomeService.getIncomeById(3L);

        assertEquals(3L, response.getIncomeId());
        assertEquals(new BigDecimal("1500"), response.getAmount());
    }
}
