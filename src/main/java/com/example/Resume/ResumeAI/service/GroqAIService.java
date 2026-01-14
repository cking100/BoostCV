package com.example.Resume.ResumeAI.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class GroqAIService {
    
    @Value("${REMOVED}")
    private String groqApiKey;
    
    @Value("${groq.api.url:https://api.groq.com/openai/v1}")
    private String groqUrl;
    
    @Value("${groq.model:llama-3.1-70b-versatile}")
    private String model;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GroqAIService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }
    
    public String generateResumeImprovement(String resumeText, String jobDescription) {
        String prompt = buildImprovementPrompt(resumeText, jobDescription);
        return callGroq(prompt, "You are an expert resume writer and ATS specialist.");
    }
    
    public String analyzeMissingKeywords(String resumeText, String jobDescription) {
        String prompt = String.format(
            "Analyze this job description and resume. List ONLY the technical keywords and skills from the job that are missing in the resume.\n\n" +
            "Job Description:\n%s\n\n" +
            "Resume:\n%s\n\n" +
            "Missing Keywords (comma-separated):",
            jobDescription, resumeText
        );
        return callGroq(prompt, "You are a keyword extraction specialist.");
    }
    
    public String checkGrammar(String resumeText) {
        String prompt = String.format(
            "Review this resume text for grammar, spelling, and style issues. List each issue with a suggested fix.\n\n" +
            "Resume:\n%s\n\n" +
            "Issues:",
            resumeText
        );
        return callGroq(prompt, "You are a professional editor and grammar expert.");
    }
    
    public String generateContentSuggestions(String resumeText, String jobDescription) {
        String prompt = String.format(
            "As a resume expert, provide 5 specific actionable suggestions to improve this resume for the given job.\n\n" +
            "Job Description:\n%s\n\n" +
            "Resume:\n%s\n\n" +
            "Top 5 Suggestions:",
            jobDescription, resumeText
        );
        return callGroq(prompt, "You are a career counselor and resume optimization expert.");
    }
    
    public Map<String, Object> analyzeResumeForATS(String resumeText) {
        String prompt = String.format(
            "Analyze this resume for ATS compatibility. Respond ONLY with valid JSON in this exact format:\n" +
            "{\n" +
            "  \"atsScore\": <number 0-100>,\n" +
            "  \"contactInfo\": {\"score\": <0-100>, \"issues\": [\"issue1\", \"issue2\"]},\n" +
            "  \"formatting\": {\"score\": <0-100>, \"issues\": []},\n" +
            "  \"keywords\": {\"score\": <0-100>, \"issues\": []},\n" +
            "  \"experience\": {\"score\": <0-100>, \"issues\": []},\n" +
            "  \"education\": {\"score\": <0-100>, \"issues\": []},\n" +
            "  \"recommendations\": [\"rec1\", \"rec2\", \"rec3\"]\n" +
            "}\n\n" +
            "Resume:\n%s",
            resumeText
        );
        
        try{
            String response = callGroqJSON(prompt);
            return objectMapper.readValue(response, Map.class);
        }catch(Exception e){
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("atsScore", 0);
            errorResponse.put("error", "Failed to parse AI response: " + e.getMessage());
            return errorResponse;
        }
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
    
    private String callGroq(String prompt, String systemMessage) {
        try{
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);
            requestBody.put("max_tokens", 2048);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            Request request = new Request.Builder()
                .url(groqUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + groqApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
            
            try(Response response = httpClient.newCall(request).execute()){
                if(!response.isSuccessful()){
                    return "Error: Failed to get AI response. Status: " + response.code();
                }
                
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if(choices != null && !choices.isEmpty()){
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
                
                return "Error: No response from AI";
            }
            
        }catch(IOException e){
            return "Error: " + e.getMessage();
        }
    }
    
    private String callGroqJSON(String prompt) {
        try{
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a resume analyzer. Always respond with valid JSON only, no markdown or extra text.");
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 2048);
            
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            Request request = new Request.Builder()
                .url(groqUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + groqApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
            
            try(Response response = httpClient.newCall(request).execute()){
                if(!response.isSuccessful()){
                    throw new IOException("API call failed with status: " + response.code());
                }
                
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if(choices != null && !choices.isEmpty()){
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
                
                throw new IOException("No response from AI");
            }
            
        }catch(IOException e){
            throw new RuntimeException("Groq API error: " + e.getMessage(), e);
        }
    }
    
    public boolean isAvailable() {
        try{
            Request request = new Request.Builder()
                .url(groqUrl + "/models")
                .addHeader("Authorization", "Bearer " + groqApiKey)
                .get()
                .build();
            
            try(Response response = httpClient.newCall(request).execute()){
                return response.isSuccessful();
            }
        }catch(IOException e){
            return false;
        }
    }
}