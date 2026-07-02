package com.expensia.backend.service.report;

import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.model.entity.*;
import com.expensia.backend.model.enums.GoalStatus;
import com.expensia.backend.model.enums.RiskPreference;
import com.expensia.backend.repository.*;
import com.expensia.backend.service.ai.AIServiceClient;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private IncomeRepository incomeRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private AIServiceClient aiServiceClient;
    @Mock private WalletRepository walletRepository;

    @InjectMocks private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .name("Test User")
                .riskPreference(RiskPreference.MEDIUM)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Expense expense(String categoryName, BigDecimal amount) {
        Expense e = new Expense();
        e.setExpenseId(1L);
        e.setUserId(1L);
        e.setAmount(amount);
        e.setCategoryName(categoryName);
        e.setDate(LocalDateTime.of(2026, 7, 1, 10, 0));
        e.setDescription("Test expense");
        e.setMerchant("Store");
        e.setPaymentMethod("CASH");
        e.setIsRecurring(false);
        e.setRecurringActive(false);
        e.setCreatedByVoice(false);
        return e;
    }

    private Income income(BigDecimal amount) {
        Income i = new Income();
        i.setIncomeId(1L);
        i.setUserId(1L);
        i.setAmount(amount);
        i.setSource("Salary");
        i.setDate(LocalDateTime.of(2026, 7, 1, 9, 0));
        i.setIsRecurring(false);
        i.setRecurringActive(false);
        return i;
    }

    private SavingGoal goal(String name) {
        SavingGoal g = new SavingGoal();
        g.setGoalId(1L);
        g.setUserId(1L);
        g.setName(name);
        g.setTargetAmount(new BigDecimal("5000"));
        g.setCurrentAmount(new BigDecimal("1000"));
        g.setDeadline(LocalDate.of(2026, 12, 31));
        g.setStatus(GoalStatus.ACTIVE);
        return g;
    }

    private Wallet wallet(BigDecimal savings) {
        Wallet w = new Wallet();
        w.setWalletId(1L);
        w.setUserId(1L);
        w.setCurrentSavings(savings);
        return w;
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

    // ── generateMonthlyReport ─────────────────────────────────────────────────

    @Test
    void generateMonthlyReport_withExpensesAndIncomes_returnsCorrectTotals() {
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        expense("Food", new BigDecimal("200")),
                        expense("Transport", new BigDecimal("50"))
                ));
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(income(new BigDecimal("3000"))));

        ReportResponse result = reportService.generateMonthlyReport();

        assertEquals(new BigDecimal("3000"), result.getTotalIncome());
        assertEquals(new BigDecimal("250"), result.getTotalExpenses());
        assertEquals(new BigDecimal("2750"), result.getBalance());
        assertTrue(result.getCategoryBreakdown().containsKey("Food"));
        assertTrue(result.getCategoryBreakdown().containsKey("Transport"));
        assertEquals(new BigDecimal("200"), result.getCategoryBreakdown().get("Food"));
    }

    @Test
    void generateMonthlyReport_nullCategoryName_groupsUnderOther() {
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expense(null, new BigDecimal("100"))));
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ReportResponse result = reportService.generateMonthlyReport();

        assertTrue(result.getCategoryBreakdown().containsKey("Other"));
        assertEquals(new BigDecimal("100"), result.getCategoryBreakdown().get("Other"));
    }

    @Test
    void generateMonthlyReport_emptyData_returnsZeros() {
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ReportResponse result = reportService.generateMonthlyReport();

        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertTrue(result.getCategoryBreakdown().isEmpty());
    }

    // ── exportCsv ─────────────────────────────────────────────────────────────

    @Test
    void exportCsv_allTime_containsHeadersAndUserInfo() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(expense("Food", new BigDecimal("100"))));
        when(incomeRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(income(new BigDecimal("2000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(List.of(goal("Car")));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet(new BigDecimal("500"))));

        String csv = reportService.exportCsv(null, null);

        assertTrue(csv.contains("EXPENSIA FINANCIAL REPORT"));
        assertTrue(csv.contains("Test User"));
        assertTrue(csv.contains("test@example.com"));
        assertTrue(csv.contains("All Time"));
        assertTrue(csv.contains("EXPENSES"));
        assertTrue(csv.contains("INCOME"));
        assertTrue(csv.contains("SAVING GOALS"));
        assertTrue(csv.contains("Food"));
        assertTrue(csv.contains("Car"));
    }

    @Test
    void exportCsv_allTime_usesOrderByDescQuery() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        reportService.exportCsv(null, null);

        verify(expenseRepository).findByUserIdOrderByDateDesc(1L);
        verify(incomeRepository).findByUserIdOrderByDateDesc(1L);
        verify(expenseRepository, never()).findByUserIdAndDateBetween(any(), any(), any());
    }

    @Test
    void exportCsv_withDateFilter_usesBetweenQueryAndShowsDateRange() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        String csv = reportService.exportCsv(start, end);

        verify(expenseRepository).findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(expenseRepository, never()).findByUserIdOrderByDateDesc(any());
        assertTrue(csv.contains("2026-06-01 to 2026-06-30"));
    }

    @Test
    void exportCsv_withDateFilter_containsExpenseAndIncomeAmounts() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expense("Food", new BigDecimal("75.50"))));
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(income(new BigDecimal("5000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet(new BigDecimal("1000"))));

        String csv = reportService.exportCsv(start, end);

        assertTrue(csv.contains("75.50"));
        assertTrue(csv.contains("5000"));
        assertTrue(csv.contains("Food"));
        assertTrue(csv.contains("Salary"));
    }

    // ── exportPdf ─────────────────────────────────────────────────────────────

    @Test
    void exportPdf_allTime_returnsValidPdfBytes() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(expense("Food", new BigDecimal("100"))));
        when(incomeRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(income(new BigDecimal("2000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(List.of(goal("Car")));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet(new BigDecimal("500"))));

        byte[] result = reportService.exportPdf(null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals('%', (char) result[0]);
        assertEquals('P', (char) result[1]);
        assertEquals('D', (char) result[2]);
        assertEquals('F', (char) result[3]);
    }

    @Test
    void exportPdf_withDateFilter_returnsNonEmptyBytes() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        byte[] result = reportService.exportPdf(start, end);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportPdf_withDateFilter_usesBetweenQuery() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        reportService.exportPdf(start, end);

        verify(expenseRepository).findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(expenseRepository, never()).findByUserIdOrderByDateDesc(any());
    }

    // ── getRecommendations ────────────────────────────────────────────────────

    @Test
    void getRecommendations_aiReturnsSuccess_returnsResponse() {
        AIRecommendationResponse aiResponse = new AIRecommendationResponse();
        setField(aiResponse, "success", true);
        setField(aiResponse, "overallScore", 0.85);

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expense("Food", new BigDecimal("200"))));
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(income(new BigDecimal("3000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(List.of(goal("Car")));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet(new BigDecimal("500"))));
        when(aiServiceClient.getRecommendations(any())).thenReturn(aiResponse);

        AIRecommendationResponse result = reportService.getRecommendations();

        assertTrue(result.isSuccess());
        assertEquals(0.85, result.getOverallScore());
    }

    @Test
    void getRecommendations_aiReturnsNull_throwsAIServiceException() {
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(aiServiceClient.getRecommendations(any())).thenReturn(null);

        assertThrows(AIServiceException.class, () -> reportService.getRecommendations());
    }

    @Test
    void getRecommendations_aiReturnsNotSuccess_throwsAIServiceException() {
        AIRecommendationResponse notSuccess = new AIRecommendationResponse();

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(aiServiceClient.getRecommendations(any())).thenReturn(notSuccess);

        assertThrows(AIServiceException.class, () -> reportService.getRecommendations());
    }

    @Test
    void getRecommendations_buildsRequestWithUserData() {
        AIRecommendationResponse aiResponse = new AIRecommendationResponse();
        setField(aiResponse, "success", true);

        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expense("Food", new BigDecimal("300"))));
        when(incomeRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(income(new BigDecimal("4000"))));
        when(goalRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet(new BigDecimal("1000"))));
        when(aiServiceClient.getRecommendations(any())).thenReturn(aiResponse);

        reportService.getRecommendations();

        verify(aiServiceClient).getRecommendations(argThat(req ->
                req.containsKey("user_id") &&
                req.containsKey("monthly_income") &&
                req.containsKey("monthly_expenses") &&
                req.containsKey("current_savings")
        ));
    }

    // ── getBenchmarks ─────────────────────────────────────────────────────────

    @Test
    void getBenchmarks_aiReturnsValue_returnsIt() {
        Map<String, Object> benchmarks = Map.of("avg_savings_rate", 0.2);
        when(aiServiceClient.getBenchmarks()).thenReturn(benchmarks);

        Object result = reportService.getBenchmarks();

        assertNotNull(result);
        assertSame(benchmarks, result);
    }

    @Test
    void getBenchmarks_aiReturnsNull_throwsAIServiceException() {
        when(aiServiceClient.getBenchmarks()).thenReturn(null);

        assertThrows(AIServiceException.class, () -> reportService.getBenchmarks());
    }
}
