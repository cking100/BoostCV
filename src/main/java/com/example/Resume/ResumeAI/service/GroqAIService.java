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
public class GroqAIService {

    private static final Logger logger = LoggerFactory.getLogger(GroqAIService.class);

    @Value("${groq.api.key:#{null}}")
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
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public boolean isAvailable() {
        return groqApiKey != null && !groqApiKey.trim().isEmpty() && !groqApiKey.equals("mock-key");
    }

    public String generateResumeImprovement(String resumeText, String jobDescription) {
        String prompt = buildImprovementPrompt(resumeText, jobDescription);
        return callGroq(prompt, "You are an expert resume writer and ATS specialist.", false);
    }

    public String analyzeMissingKeywords(String resumeText, String jobDescription) {
        String prompt = String.format(
                "Analyze this job description and resume. List ONLY the technical keywords and skills " +
                "from the job that are MISSING in the resume.\n\n" +
                "Job Description:\n%s\n\n" +
                "Resume:\n%s\n\n" +
                "Missing Keywords (comma-separated):",
                jobDescription, resumeText
        );
        return callGroq(prompt, "You are a keyword extraction specialist.", false);
    }

    public String checkGrammar(String resumeText) {
        String prompt = String.format(
                "Review this resume text for grammar, spelling, and style issues. " +
                "List each issue with a suggested fix.\n\n" +
                "Resume:\n%s\n\n" +
                "Issues:",
                resumeText
        );
        return callGroq(prompt, "You are a professional editor and grammar expert.", false);
    }

    public String generateContentSuggestions(String resumeText, String jobDescription) {
        String prompt = String.format(
                "As a resume expert, provide 5 specific actionable suggestions to improve this resume " +
                "for the given job.\n\n" +
                "Job Description:\n%s\n\n" +
                "Resume:\n%s\n\n" +
                "Top 5 Suggestions:",
                jobDescription, resumeText
        );
        return callGroq(prompt, "You are a career counselor and resume optimization expert.", false);
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

        try {
            String response = callGroq(prompt,
                    "You are a resume analyzer. Always respond with valid JSON only, no markdown formatting.", true);

            // Clean markdown blocks if LLM output includes ```json and ```
            if (response.contains("```json")) {
                response = response.substring(response.indexOf("```json") + 7);
                if (response.contains("```")) {
                    response = response.substring(0, response.indexOf("```"));
                }
            } else if (response.contains("```")) {
                response = response.substring(response.indexOf("```") + 3);
                if (response.contains("```")) {
                    response = response.substring(0, response.indexOf("```"));
                }
            }

            return objectMapper.readValue(response.trim(), Map.class);
        } catch (Exception e) {
            logger.error("Failed to parse ATS JSON analysis response from Groq", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("atsScore", 0);
            errorResponse.put("error", "Failed to parse AI response: " + e.getMessage());
            return errorResponse;
        }
    }

    public int calculateMatchScore(String resumeText, String jobText) {
        String prompt = String.format(
                "How well does this resume match the job? Rate from 0-100.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\n" +
                "Respond with ONLY a single integer number between 0 and 100. No text, no explanation.",
                truncate(jobText, 2000), truncate(resumeText, 3000)
        );
        String response = callGroq(prompt, "You are a resume evaluator. Output only a number.", false);
        String digits = response.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                int score = Integer.parseInt(digits.substring(0, Math.min(digits.length(), 3)));
                return Math.min(100, Math.max(0, score));
            } catch (NumberFormatException ignored) {}
        }
        return 65;
    }

    public String analyzeMatchedKeywords(String resumeText, String jobDescription) {
        String prompt = String.format(
                "List technical keywords and skills that appear in BOTH the job description AND the resume. " +
                "Return ONLY a comma-separated list, no other text.\n\n" +
                "Job Description:\n%s\n\nResume:\n%s\n\nMatched keywords:",
                jobDescription, resumeText
        );
        return callGroq(prompt, "You are a keyword extraction specialist.", false);
    }

    public String generateCoverLetter(String resumeText, String jobTitle,
                                      String company, String jobDescription) {
        String prompt = String.format(
                "Write a compelling, professional cover letter for this candidate applying to the following role.\n\n" +
                "Job Title: %s\n" +
                "Company: %s\n" +
                "Job Description:\n%s\n\n" +
                "Candidate's Resume:\n%s\n\n" +
                "Instructions:\n" +
                "- Address it to 'Hiring Manager' if no specific contact is known\n" +
                "- Open with a strong hook referencing the role\n" +
                "- Map 2-3 specific resume achievements directly to job requirements\n" +
                "- Keep it under 400 words\n" +
                "- End with a confident call to action\n\n" +
                "Cover Letter:",
                jobTitle, company != null ? company : "the company",
                truncate(jobDescription, 1500), truncate(resumeText, 3000)
        );
        return callGroq(prompt,
                "You are an expert career coach and professional cover letter writer. " +
                "Write in a natural, confident, first-person tone.", false);
    }

    public String generateInterviewQuestions(String resumeText, String jobDescription) {
        String prompt = String.format(
                "Generate 10 likely interview questions for this candidate applying to this job, " +
                "along with concise model answers based on the candidate's actual resume.\n\n" +
                "Job Description:\n%s\n\n" +
                "Candidate Resume:\n%s\n\n" +
                "Format each entry as:\n" +
                "Q1: [question]\n" +
                "A1: [model answer drawing from resume]\n\n" +
                "Mix behavioral, technical, and situational questions. " +
                "Answers should reference real skills and experiences from the resume.\n\n" +
                "Interview Q&A:",
                truncate(jobDescription, 1500), truncate(resumeText, 3000)
        );
        return callGroq(prompt,
                "You are an experienced technical interviewer and career coach.", false);
    }

    public String suggestCareerPaths(String resumeText) {
        String prompt = String.format(
                "Based on this candidate's resume, suggest 3 distinct career paths they could pursue. " +
                "For each path provide:\n" +
                "1. Path name and why it suits them\n" +
                "2. Required skill gaps to close\n" +
                "3. A 90-day action plan with concrete steps\n" +
                "4. Example job titles to target\n\n" +
                "Resume:\n%s\n\n" +
                "Format clearly with headers for each path. Be specific and actionable.\n\n" +
                "Career Path Suggestions:",
                truncate(resumeText, 4000)
        );
        return callGroq(prompt,
                "You are a senior career strategist and executive coach with 20 years of experience " +
                "across tech, finance, and business domains.", false);
    }

    private String buildImprovementPrompt(String resumeText, String jobDescription) {
        return String.format(
                "You are an expert resume writer and ATS specialist. Analyze this resume against the " +
                "job description and provide:\n" +
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

    private String callGroq(String prompt, String systemMessage, boolean forceJson) {
        if (!isAvailable()) {
            logger.warn("Groq API key is not configured. Returning fallback static description.");
            return forceJson
                ? "{\"atsScore\": 75, \"contactInfo\": {\"score\": 90, \"issues\": []}, " +
                  "\"formatting\": {\"score\": 85, \"issues\": []}, " +
                  "\"keywords\": {\"score\": 70, \"issues\": [\"Add docker\"]}, " +
                  "\"experience\": {\"score\": 80, \"issues\": []}, " +
                  "\"education\": {\"score\": 90, \"issues\": []}, " +
                  "\"recommendations\": [\"Add quantitative achievements\"]}"
                : "Fallback Content: Groq API key not configured. Please set GROQ_API_KEY environment variable.";
        }

        try {
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
            requestBody.put("temperature", forceJson ? 0.3 : 0.7);
            requestBody.put("max_tokens", 2048);

            if (forceJson) {
                Map<String, String> responseFormat = new HashMap<>();
                responseFormat.put("type", "json_object");
                requestBody.put("response_format", responseFormat);
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(groqUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + groqApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Groq API call failed with status: {}", response.code());
                    return "Error: Failed to obtain Groq response. Status code: " + response.code();
                }

                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
                return "Error: Empty response content from Groq API.";
            }

        } catch (IOException e) {
            logger.error("IOException encountered while querying Groq API: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
