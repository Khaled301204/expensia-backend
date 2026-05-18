package com.expensia.backend.service.ai;

import com.expensia.backend.dto.response.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class AIServiceClient {

    private final RestTemplate restTemplate;

    public AIServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public AICategorizationResponse categorizeExpense(String description, String merchant) {
        try {
            String url = "http://localhost:8000/api/categorize";

            Map<String, String> request = Map.of(
                    "description", description == null ? "" : description,
                    "merchant", merchant == null ? "" : merchant
            );

            ResponseEntity<AICategorizationResponse> response =
                    restTemplate.postForEntity(url, request, AICategorizationResponse.class);

            if (response.getBody() == null || !response.getBody().isSuccess()) {
                return fallback();
            }

            return response.getBody();

        } catch (Exception e) {
            System.out.println("AI service unavailable: " + e.getMessage());
            return fallback();
        }
    }

    private AICategorizationResponse fallback() {
        return new AICategorizationResponse() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getCategory() {
                return "Other";
            }

            @Override
            public Double getConfidence() {
                return 0.0;
            }
        };
    }

    public NLPParseResponse parseExpenseText(String text) {

        try {

            String url = "http://localhost:8000/api/parse-and-categorize";

            Map<String, String> request = Map.of(
                    "text", text
            );

            ResponseEntity<NLPParseResponse> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            NLPParseResponse.class
                    );

            return response.getBody();

        } catch (Exception e) {

            System.out.println("NLP service unavailable: " + e.getMessage());

            return null;
        }
    }

    public AIRecommendationResponse getRecommendations(Map<String, Object> request) {
        try {
            String url = "http://localhost:8000/api/recommend";

            ResponseEntity<AIRecommendationResponse> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            AIRecommendationResponse.class
                    );

            return response.getBody();

        } catch (Exception e) {
            System.out.println("Recommendation service unavailable: " + e.getMessage());
            return null;
        }
    }

    public AIInsightsResponse getCompleteInsights(Map<String, Object> request) {
        try {
            String url = "http://localhost:8000/api/insights";

            ResponseEntity<AIInsightsResponse> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            AIInsightsResponse.class
                    );

            return response.getBody();

        } catch (Exception e) {
            System.out.println("Insights service unavailable: " + e.getMessage());
            return null;
        }
    }

    public VoiceExpenseResponse speechToExpense(
            MultipartFile audio,
            String language
    ) {
        try {
            String url = "http://localhost:8000/api/speech-to-expense";

            ByteArrayResource audioResource = new ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    return audio.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", audioResource);
            body.add("language", language == null ? "auto" : language);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<VoiceExpenseResponse> response =
                    restTemplate.postForEntity(
                            url,
                            requestEntity,
                            VoiceExpenseResponse.class
                    );

            return response.getBody();

        } catch (Exception e) {
            System.out.println("Speech service unavailable: " + e.getMessage());
            return null;
        }
    }
}