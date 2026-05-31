package com.example.Resume.ResumeAI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${gemini.api.key:#{null}}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiUrl;

    @Value("${gemini.embeddings.model:text-embedding-004}")
    private String embeddingsModel;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EmbeddingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Generates a vector embedding for the given text.
     * If the API fails or is unconfigured, falls back to a deterministic mock vector.
     */
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return generateFallbackVector("", 768);
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("GEMINI_API_KEY") || apiKey.equals("mock-key")) {
            logger.warn("Gemini API key not configured. Using deterministic fallback embeddings.");
            return generateFallbackVector(text, 768);
        }

        try {
            // Build Gemini Embed Request: { "content": { "parts": [ { "text": "..." } ] } }
            Map<String, Object> requestBodyMap = new HashMap<>();
            
            Map<String, Object> contentMap = new HashMap<>();
            List<Map<String, Object>> partsList = new ArrayList<>();
            Map<String, Object> partMap = new HashMap<>();
            partMap.put("text", text);
            partsList.add(partMap);
            contentMap.put("parts", partsList);
            
            requestBodyMap.put("content", contentMap);

            String jsonBody = objectMapper.writeValueAsString(requestBodyMap);

            String url = String.format("%s/models/%s:embedContent?key=%s", geminiUrl, embeddingsModel, apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                    Map<String, Object> embeddingObject = (Map<String, Object>) responseMap.get("embedding");
                    
                    if (embeddingObject != null) {
                        List<Double> values = (List<Double>) embeddingObject.get("values");
                        if (values != null) {
                            return values;
                        }
                    }
                }
                logger.warn("Embedding API call failed with status: {}. Using fallback.", response.code());
            }
        } catch (Exception e) {
            logger.error("Error generating vector embedding from Gemini API: {}", e.getMessage());
        }

        return generateFallbackVector(text, 768);
    }

    /**
     * Helper to serialize vector to a standard vector text format: [x,y,z,...]
     */
    public String serializeVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.size(); i++) {
            sb.append(vector.get(i));
            if (i < vector.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * De-serializes vector string back into List<Double> for calculations
     */
    public List<Double> deserializeVector(String vectorStr) {
        List<Double> vector = new ArrayList<>();
        if (vectorStr == null || vectorStr.length() < 3) {
            return vector;
        }
        try {
            String cleaned = vectorStr.substring(1, vectorStr.length() - 1);
            String[] elements = cleaned.split(",");
            for (String el : elements) {
                vector.add(Double.parseDouble(el.trim()));
            }
        } catch (Exception e) {
            logger.error("Failed to deserialize vector string: {}", vectorStr, e);
        }
        return vector;
    }

    /**
     * Generates a deterministic mock vector based on the string hash to support local tests.
     */
    private List<Double> generateFallbackVector(String text, int dimensions) {
        List<Double> vector = new ArrayList<>(dimensions);
        int seed = text.hashCode();
        java.util.Random random = new java.util.Random(seed);
        double sumSquare = 0.0;

        for (int i = 0; i < dimensions; i++) {
            double val = random.nextGaussian();
            vector.add(val);
            sumSquare += val * val;
        }

        // L2 normalize the vector
        double magnitude = Math.sqrt(sumSquare);
        if (magnitude > 0) {
            for (int i = 0; i < dimensions; i++) {
                vector.set(i, vector.get(i) / magnitude);
            }
        }

        return vector;
    }
}
