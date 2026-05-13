package com.expensia.backend.service.ai;

import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.NLPParseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}