package com.expensia.backend.service.ai;

import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.VoiceExpenseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AIServiceClientTest {

    private AIServiceClient client;
    private RestTemplate mockRestTemplate;

    @BeforeEach
    void setUp() throws Exception {
        client = new AIServiceClient();
        mockRestTemplate = mock(RestTemplate.class);

        Field urlField = AIServiceClient.class.getDeclaredField("aiBaseUrl");
        urlField.setAccessible(true);
        urlField.set(client, "http://localhost:8000");

        Field rtField = AIServiceClient.class.getDeclaredField("restTemplate");
        rtField.setAccessible(true);
        rtField.set(client, mockRestTemplate);
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── categorizeExpense ─────────────────────────────────────────────────────

    @Test
    void categorizeExpense_success_returnsAIResponse() {
        AICategorizationResponse aiResponse = new AICategorizationResponse();
        setField(aiResponse, "success", true);
        setField(aiResponse, "category", "Food");
        setField(aiResponse, "confidence", 0.95);

        when(mockRestTemplate.postForEntity(
                eq("http://localhost:8000/categorize"),
                any(),
                eq(AICategorizationResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));

        AICategorizationResponse result = client.categorizeExpense("burger", "McDonald's");

        assertTrue(result.isSuccess());
        assertEquals("Food", result.getCategory());
        assertEquals(0.95, result.getConfidence());
    }

    @Test
    void categorizeExpense_aiReturnsNullBody_returnsFallback() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AICategorizationResponse.class)
        )).thenReturn(ResponseEntity.ok(null));

        AICategorizationResponse result = client.categorizeExpense("test", "merchant");

        assertFalse(result.isSuccess());
        assertEquals("Other", result.getCategory());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void categorizeExpense_aiReturnsNotSuccess_returnsFallback() {
        AICategorizationResponse notSuccess = new AICategorizationResponse();
        // success defaults to false

        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AICategorizationResponse.class)
        )).thenReturn(ResponseEntity.ok(notSuccess));

        AICategorizationResponse result = client.categorizeExpense("test", "merchant");

        assertFalse(result.isSuccess());
        assertEquals("Other", result.getCategory());
    }

    @Test
    void categorizeExpense_timeout_returnsFallback() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AICategorizationResponse.class)
        )).thenThrow(new ResourceAccessException("Connection timed out"));

        AICategorizationResponse result = client.categorizeExpense("pizza", "Pizza Hut");

        assertFalse(result.isSuccess());
        assertEquals("Other", result.getCategory());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void categorizeExpense_connectionRefused_returnsFallback() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AICategorizationResponse.class)
        )).thenThrow(new RestClientException("Connection refused"));

        AICategorizationResponse result = client.categorizeExpense("shoes", "Nike");

        assertFalse(result.isSuccess());
        assertEquals("Other", result.getCategory());
    }

    // ── speechToExpense ───────────────────────────────────────────────────────

    @Test
    void speechToExpense_success_returnsResponse() throws Exception {
        VoiceExpenseResponse voiceResponse = new VoiceExpenseResponse();
        setField(voiceResponse, "success", true);
        setField(voiceResponse, "amount", 50.0);
        setField(voiceResponse, "merchant", "Coffee Shop");

        when(mockRestTemplate.postForEntity(
                eq("http://localhost:8000/speech-to-expense"),
                any(),
                eq(VoiceExpenseResponse.class)
        )).thenReturn(ResponseEntity.ok(voiceResponse));

        MockMultipartFile audio = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "audio-bytes".getBytes()
        );

        VoiceExpenseResponse result = client.speechToExpense(audio, "en");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(50.0, result.getAmount());
        assertEquals("Coffee Shop", result.getMerchant());
    }

    @Test
    void speechToExpense_serviceUnavailable_returnsNull() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(VoiceExpenseResponse.class)
        )).thenThrow(new RestClientException("Service unavailable"));

        MockMultipartFile audio = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "audio-bytes".getBytes()
        );

        VoiceExpenseResponse result = client.speechToExpense(audio, "en");

        assertNull(result);
    }

    // ── getRecommendations ────────────────────────────────────────────────────

    @Test
    void getRecommendations_success_returnsResponse() {
        AIRecommendationResponse aiResponse = new AIRecommendationResponse();
        setField(aiResponse, "success", true);
        setField(aiResponse, "overallScore", 0.88);

        when(mockRestTemplate.postForEntity(
                eq("http://localhost:8000/recommend"),
                any(),
                eq(AIRecommendationResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));

        AIRecommendationResponse result = client.getRecommendations(new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0.88, result.getOverallScore());
    }

    @Test
    void getRecommendations_timeout_returnsNull() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AIRecommendationResponse.class)
        )).thenThrow(new ResourceAccessException("Read timed out"));

        AIRecommendationResponse result = client.getRecommendations(Map.of("user_id", 1L));

        assertNull(result);
    }

    @Test
    void getRecommendations_connectionRefused_returnsNull() {
        when(mockRestTemplate.postForEntity(
                anyString(), any(), eq(AIRecommendationResponse.class)
        )).thenThrow(new RestClientException("Connection refused"));

        AIRecommendationResponse result = client.getRecommendations(new HashMap<>());

        assertNull(result);
    }

    // ── getBenchmarks ─────────────────────────────────────────────────────────

    @Test
    void getBenchmarks_success_returnsResponseBody() {
        Map<String, Object> benchmarks = Map.of("avg_savings_rate", 0.2, "avg_expense_ratio", 0.6);
        when(mockRestTemplate.getForEntity(
                eq("http://localhost:8000/benchmarks"),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(benchmarks));

        Object result = client.getBenchmarks();

        assertNotNull(result);
        assertSame(benchmarks, result);
    }

    @Test
    void getBenchmarks_serviceDown_returnsNull() {
        when(mockRestTemplate.getForEntity(
                anyString(), eq(Object.class)
        )).thenThrow(new RestClientException("Connection refused"));

        Object result = client.getBenchmarks();

        assertNull(result);
    }
}
