package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ConfirmVoiceExpenseRequest;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.dto.response.VoiceExpensePreviewResponse;
import com.expensia.backend.dto.response.VoiceExpenseResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.service.wallet.WalletService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VoiceExpenseService {

    private final AIServiceClient aiServiceClient;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final WalletService walletService;

    public VoiceExpenseService(
            AIServiceClient aiServiceClient,
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            WalletService walletService
    ) {
        this.aiServiceClient = aiServiceClient;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.walletService = walletService;
    }

    public ExpenseResponse createFromVoice(
            MultipartFile audio,
            String language
    ) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        VoiceExpenseResponse aiResponse =
                aiServiceClient.speechToExpense(audio, language);

        if (aiResponse == null || !aiResponse.isSuccess()) {
            throw new AIServiceException("Could not process voice expense");
        }

        Expense expense = new Expense();

        expense.setUserId(currentUser.getUserId());

        expense.setAmount(
                BigDecimal.valueOf(aiResponse.getAmount())
        );

        expense.setDescription(aiResponse.getDescription());
        expense.setMerchant(aiResponse.getMerchant());

        expense.setCategoryName(aiResponse.getCategory());

        expense.setCategoryConfidence(
                aiResponse.getConfidence() == null
                        ? 0.0
                        : aiResponse.getConfidence()
        );

        Category category = categoryRepository
                .findByNameIgnoreCase(aiResponse.getCategory())
                .orElse(null);

        if (category != null) {
            expense.setCategoryId(category.getCategoryId());
        }

        if (aiResponse.getDate() != null) {
            expense.setDate(
                    LocalDate.parse(aiResponse.getDate()).atStartOfDay()
            );
        } else {
            expense.setDate(LocalDateTime.now());
        }

        expense.setPaymentMethod("CASH");
        expense.setIsRecurring(false);
        expense.setCreatedByVoice(true);

        Expense savedExpense = expenseRepository.save(expense);

        walletService.decreaseSavings(
                currentUser.getUserId(),
                savedExpense.getAmount()
        );

        return new ExpenseResponse(
                savedExpense.getExpenseId(),
                savedExpense.getUserId(),
                savedExpense.getAmount(),
                savedExpense.getCategoryId(),
                savedExpense.getCategoryName(),
                savedExpense.getCategoryConfidence(),
                savedExpense.getDate(),
                savedExpense.getDescription(),
                savedExpense.getMerchant(),
                savedExpense.getPaymentMethod(),
                savedExpense.getIsRecurring(),
                savedExpense.getCreatedByVoice(),
                savedExpense.getCreatedAt()
        );
    }

    public VoiceExpensePreviewResponse previewFromVoice(
            MultipartFile audio,
            String language
    ) {
        VoiceExpenseResponse aiResponse =
                aiServiceClient.speechToExpense(audio, language);

        if (aiResponse == null || !aiResponse.isSuccess()) {
            throw new AIServiceException("Could not process voice expense");
        }

        String categoryName = aiResponse.getCategory();
        Double confidence = aiResponse.getConfidence();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        Category category = categoryRepository
                .findByNameIgnoreCase(categoryName)
                .orElse(null);

        Long categoryId = category != null
                ? category.getCategoryId()
                : null;

        return new VoiceExpensePreviewResponse(
                aiResponse.getAmount(),
                aiResponse.getMerchant(),
                aiResponse.getDescription(),
                aiResponse.getDate(),
                categoryId,
                categoryName,
                confidence == null ? 0.0 : confidence,
                aiResponse.getSpeechMetadata()
        );
    }

    public ExpenseResponse confirmVoiceExpense(
            ConfirmVoiceExpenseRequest request
    ) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Expense expense = new Expense();

        expense.setUserId(currentUser.getUserId());

        expense.setAmount(request.getAmount());

        expense.setCategoryId(request.getCategoryId());
        expense.setCategoryName(request.getCategoryName());

        expense.setCategoryConfidence(
                request.getCategoryConfidence() == null
                        ? 0.0
                        : request.getCategoryConfidence()
        );

        expense.setDate(request.getDate());

        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());

        expense.setPaymentMethod(
                request.getPaymentMethod() == null
                        ? "CASH"
                        : request.getPaymentMethod()
        );

        expense.setIsRecurring(false);
        expense.setCreatedByVoice(true);

        Expense savedExpense = expenseRepository.save(expense);

        walletService.decreaseSavings(
                currentUser.getUserId(),
                savedExpense.getAmount()
        );

        return new ExpenseResponse(
                savedExpense.getExpenseId(),
                savedExpense.getUserId(),
                savedExpense.getAmount(),
                savedExpense.getCategoryId(),
                savedExpense.getCategoryName(),
                savedExpense.getCategoryConfidence(),
                savedExpense.getDate(),
                savedExpense.getDescription(),
                savedExpense.getMerchant(),
                savedExpense.getPaymentMethod(),
                savedExpense.getIsRecurring(),
                savedExpense.getCreatedByVoice(),
                savedExpense.getCreatedAt()
        );
    }
}