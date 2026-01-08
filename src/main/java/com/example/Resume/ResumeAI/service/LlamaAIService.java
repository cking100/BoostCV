package com.example.Resume.ResumeAI.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class LlamaAIService {
    
    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:llama3}")
    private String model;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public LlamaAIService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }
    
    public String generateResumeImprovement(String resumeText, String jobDescription) {
        String prompt = buildImprovementPrompt(resumeText, jobDescription);
        return callOllama(prompt);
    }
    
    public String analyzeMissingKeywords(String resumeText, String jobDescription) {
        String prompt = String.format(
            "Analyze this job description and resume. List ONLY the technical keywords and skills from the job that are missing in the resume.\n\n" +
            "Job Description:\n%s\n\n" +
            "Resume:\n%s\n\n" +
            "Missing Keywords (comma-separated):",
            jobDescription, resumeText
        );
        return callOllama(prompt);
    }
    
    public String checkGrammar(String resumeText) {
        String prompt = String.format(
            "Review this resume text for grammar, spelling, and style issues. List each issue with a suggested fix.\n\n" +
            "Resume:\n%s\n\n" +
            "Issues:",
            resumeText
        );
        return callOllama(prompt);
    }
    
    public String generateContentSuggestions(String resumeText, String jobDescription) {
        String prompt = String.format(
            "As a resume expert, provide 5 specific actionable suggestions to improve this resume for the given job.\n\n" +
            "Job Description:\n%s\n\n" +
            "Resume:\n%s\n\n" +
            "Top 5 Suggestions:",
            jobDescription, resumeText
        );
        return callOllama(prompt);
    }
    
    private String buildImprovementPrompt(String resumeText, String jobDescription) {
        return String.format(
            "You are an expert resume writer and ATS specialist. Analyze this resume against the job description and provide:\n" +
            "1. Overall assessment (1-2 sentences)\n" +
            "2. Top 3 strengths\n" +
            "3. Top 3 weaknesses\n" +
            "4. Specific action items to improve ATS score\n\n" +
            "Job Description:\n%s\n\n" +
            "Resume:\n%s\n\n" +
            "Analysis:",
            jobDescription, resumeText
        );
    }
    
    private String callOllama(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            Request request = new Request.Builder()
                .url(ollamaUrl + "/api/generate")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Error: Failed to get AI response. Make sure Ollama is running.";
                }
                
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                return (String) responseMap.get("response");
            }
            
        } catch (IOException e) {
            return "Error: " + e.getMessage() + ". Make sure Ollama is installed and running on " + ollamaUrl;
        }
    }
    
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                .url(ollamaUrl + "/api/tags")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }
}