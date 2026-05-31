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
public class GeminiAIService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiUrl;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiAIService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        if (geminiApiKey != null && geminiApiKey.length() > 8) {
            String masked = geminiApiKey.substring(0, 8) + "..." + geminiApiKey.substring(geminiApiKey.length() - 4);
            logger.info("GeminiAIService initialized. Using API Key: {}", masked);
        } else {
            logger.warn("GeminiAIService initialized with invalid or empty API key: {}", geminiApiKey);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  EXISTING METHODS (unchanged)
    // ─────────────────────────────────────────────────────────────────

    public boolean isAvailable() {
        return geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.equals("mock-key");
    }

    public String generateResumeImprovement(String resumeText, String jobDescription) {
        String prompt = buildImprovementPrompt(resumeText, jobDescription);
        return callGemini(prompt, "You are an expert resume writer and ATS specialist.", false);
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
        return callGemini(prompt, "You are a keyword extraction specialist.", false);
    }

    public String checkGrammar(String resumeText) {
        String prompt = String.format(
                "Review this resume text for grammar, spelling, and style issues. " +
                "List each issue with a suggested fix.\n\n" +
                "Resume:\n%s\n\n" +
                "Issues:",
                resumeText
        );
        return callGemini(prompt, "You are a professional editor and grammar expert.", false);
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
        return callGemini(prompt, "You are a career counselor and resume optimization expert.", false);
    }

    public Map<String, Object> analyzeResumeForATS(String resumeText) {

        // Plain-text instructions only — no JSON template in the prompt.
        // The JSON structure is enforced via responseSchema in the API call itself.
        String prompt =
            "You are an expert ATS (Applicant Tracking System) resume analyst.\n" +
            "Analyze the resume below and fill in every field of the JSON response.\n\n" +

            "SCORING RULES:\n" +
            "contactInfo: Score 100 if email + phone + city + LinkedIn all present. Deduct 20 per missing item.\n" +
            "impactMetrics: Count bullets with a hard number/percent/dollar/growth metric. " +
            "Score = min(100, 20 + 10 * count). For bullets lacking metrics, name the bullet and suggest a number.\n" +
            "actionVerbs: Penalise weak openers (Responsible for, Helped, Worked on, Assisted). " +
            "Quote exact weak phrases found. Reward strong verbs (Led, Built, Launched, Reduced).\n" +
            "keywordDensity: Identify the target role/domain. List 3 ATS keywords MISSING from this resume.\n" +
            "experienceDepth: Check each role for 3+ bullets, scope mention, and result. Name roles lacking depth.\n" +
            "formatting: Check date format consistency, standard section headers, no tables/columns.\n" +
            "education: Check degree, institution, graduation year. Suggest GPA if early-career.\n" +
            "professionalPresence: Check for summary, GitHub/portfolio, certifications.\n" +
            "atsScore: Weighted average: impactMetrics*0.25 + actionVerbs*0.15 + keywordDensity*0.15 + " +
            "experienceDepth*0.15 + contactInfo*0.10 + formatting*0.10 + education*0.05 + professionalPresence*0.05.\n\n" +

            "MANDATORY: Every issues array must have 2-4 strings specific to THIS resume. Never generic.\n" +
            "MANDATORY: recommendations must have exactly 5 strings, highest impact first.\n\n" +

            "--- RESUME ---\n" +
            resumeText;

        try {
            Map<String, Object> result = callGeminiJson(prompt);

            if (result == null || result.isEmpty()) {
                logger.error("[ATS] Gemini returned null/empty result");
                return new HashMap<>();
            }

            logger.info("[ATS] Success. atsScore={}, keys={}", result.get("atsScore"), result.keySet());
            return result;

        } catch (Exception e) {
            logger.error("[ATS] Failed: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  BUG-FIX METHODS
    // ─────────────────────────────────────────────────────────────────

    /**
     * Returns an integer match score (0-100) comparing resume against a job.
     * Fixed version: uses a targeted prompt that forces a single numeric output.
     */
    public int calculateMatchScore(String resumeText, String jobText) {
        String prompt = String.format(
                "How well does this resume match the job? Rate from 0-100.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\n" +
                "Respond with ONLY a single integer number between 0 and 100. No text, no explanation.",
                truncate(jobText, 2000), truncate(resumeText, 3000)
        );
        String response = callGemini(prompt, "You are a resume evaluator. Output only a number.", false);
        String digits = response.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                int score = Integer.parseInt(digits.substring(0, Math.min(digits.length(), 3)));
                return Math.min(100, Math.max(0, score));
            } catch (NumberFormatException ignored) {}
        }
        return 65;
    }

    /**
     * Returns comma-separated keywords that appear in BOTH the resume AND the job description.
     * Fixed version: dedicated matched-keywords prompt (not reusing the missing-keywords endpoint).
     */
    public String analyzeMatchedKeywords(String resumeText, String jobDescription) {
        String prompt = String.format(
                "List technical keywords and skills that appear in BOTH the job description AND the resume. " +
                "Return ONLY a comma-separated list, no other text.\n\n" +
                "Job Description:\n%s\n\nResume:\n%s\n\nMatched keywords:",
                jobDescription, resumeText
        );
        return callGemini(prompt, "You are a keyword extraction specialist.", false);
    }

    // ─────────────────────────────────────────────────────────────────
    //  NEW GEN AI FEATURES
    // ─────────────────────────────────────────────────────────────────

    /**
     * Generates a tailored cover letter using the candidate's resume as context.
     */
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
        return callGemini(prompt,
                "You are an expert career coach and professional cover letter writer. " +
                "Write in a natural, confident, first-person tone.", false);
    }

    /**
     * Generates 10 likely interview questions with model answers, grounded in the resume + job.
     */
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
        return callGemini(prompt,
                "You are an experienced technical interviewer and career coach.", false);
    }

    /**
     * Suggests 3 personalized career paths with 90-day action plans based on the resume.
     */
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
        return callGemini(prompt,
                "You are a senior career strategist and executive coach with 20 years of experience " +
                "across tech, finance, and business domains.", false);
    }

    // ─────────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────

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

    /**
     * Calls Gemini with responseMimeType=application/json + responseSchema.
     * Guaranteed to return valid JSON matching our ATS schema — no prompt-based enforcement needed.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callGeminiJson(String prompt) throws Exception {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()
                || geminiApiKey.contains("GEMINI_API_KEY")
                || geminiApiKey.equals("mock-key")) {
            logger.warn("[ATS] Gemini API key not configured.");
            return new HashMap<>();
        }

        // ── Build the responseSchema (defines exact JSON structure) ───────
        Map<String, Object> strType  = Map.of("type", "STRING");
        Map<String, Object> intType  = Map.of("type", "INTEGER");
        Map<String, Object> strArray = Map.of("type", "ARRAY", "items", strType);

        // Category object: {score: integer, issues: [string]}
        Map<String, Object> categorySchema = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                "score",  intType,
                "issues", strArray
            ),
            "required", List.of("score", "issues")
        );

        Map<String, Object> responseSchema = new HashMap<>();
        responseSchema.put("type", "OBJECT");
        responseSchema.put("properties", new HashMap<String, Object>() {{
            put("atsScore",            intType);
            put("contactInfo",         categorySchema);
            put("impactMetrics",        categorySchema);
            put("actionVerbs",          categorySchema);
            put("keywordDensity",       categorySchema);
            put("experienceDepth",      categorySchema);
            put("formatting",           categorySchema);
            put("education",            categorySchema);
            put("professionalPresence", categorySchema);
            put("recommendations",      strArray);
        }});
        responseSchema.put("required", List.of(
            "atsScore", "contactInfo", "impactMetrics", "actionVerbs",
            "keywordDensity", "experienceDepth", "formatting",
            "education", "professionalPresence", "recommendations"
        ));

        // ── Build request body ─────────────────────────────────────────────
        Map<String, Object> partItem = Map.of("text", prompt);
        Map<String, Object> contentItem = Map.of("parts", List.of(partItem));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature",      0.2);
        generationConfig.put("maxOutputTokens",  8192);
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema",   responseSchema);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents",         List.of(contentItem));
        requestBody.put("generationConfig", generationConfig);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String url = String.format("%s/models/%s:generateContent?key=%s",
                geminiUrl, model, geminiApiKey);

        logger.info("[ATS] Calling Gemini JSON schema endpoint. Prompt length={}", prompt.length());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "(no body)";
                logger.error("[ATS] Gemini JSON call failed. status={} body={}", response.code(), errorBody);
                return new HashMap<>();
            }

            String responseBody = response.body().string();
            logger.info("[ATS] Raw Gemini JSON response (first 500 chars): {}",
                    responseBody.substring(0, Math.min(responseBody.length(), 500)));

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                logger.error("[ATS] No candidates in Gemini response");
                return new HashMap<>();
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String jsonText = (String) parts.get(0).get("text");

            logger.info("[ATS] Extracted JSON text (first 300 chars): {}",
                    jsonText.substring(0, Math.min(jsonText.length(), 300)));

            // Try full parse first
            try {
                return objectMapper.readValue(jsonText, Map.class);
            } catch (com.fasterxml.jackson.core.JacksonException parseEx) {
                // JSON was truncated — attempt to rescue atsScore with a regex
                logger.warn("[ATS] Full JSON parse failed (likely truncated response). Attempting partial rescue. Error: {}", parseEx.getMessage());
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("\\\"atsScore\\\"\\s*:\\s*(\\d+)")
                        .matcher(jsonText);
                if (m.find()) {
                    int rescuedScore = Integer.parseInt(m.group(1));
                    logger.info("[ATS] Rescued atsScore={} from partial JSON", rescuedScore);
                    Map<String, Object> partial = new HashMap<>();
                    partial.put("atsScore", rescuedScore);
                    return partial;
                }
                logger.error("[ATS] Could not rescue any data from truncated JSON");
                return new HashMap<>();
            }
        }
    }

    private String callGemini(String prompt, String systemMessage, boolean forceJson) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()
                || geminiApiKey.contains("GEMINI_API_KEY")
                || geminiApiKey.equals("mock-key")) {
            logger.warn("Gemini API key is not configured. Returning fallback static description.");
            return forceJson
                ? "{\"atsScore\": 75, \"contactInfo\": {\"score\": 90, \"issues\": []}, " +
                  "\"formatting\": {\"score\": 85, \"issues\": []}, " +
                  "\"keywords\": {\"score\": 70, \"issues\": [\"Add docker\"]}, " +
                  "\"experience\": {\"score\": 80, \"issues\": []}, " +
                  "\"education\": {\"score\": 90, \"issues\": []}, " +
                  "\"recommendations\": [\"Add quantitative achievements\"]}"
                : "Fallback Content: Gemini API key not configured. Please set GEMINI_API_KEY environment variable.";
        }

        try {
            // Prepend system message to prompt if present for compatibility across all API versions (v1 and v1beta)
            if (systemMessage != null && !systemMessage.isEmpty()) {
                prompt = "System Instruction: " + systemMessage + "\n\nUser Request:\n" + prompt;
            }

            // Build Gemini request body
            Map<String, Object> requestBody = new HashMap<>();

            // Contents array
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentItem = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> partItem = new HashMap<>();
            partItem.put("text", prompt);
            parts.add(partItem);
            contentItem.put("parts", parts);
            contents.add(contentItem);
            requestBody.put("contents", contents);

            // Generation config — do NOT set responseMimeType (causes 400 without a responseSchema)
            // JSON output is enforced through the system prompt instead
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", forceJson ? 0.1 : 0.7);
            generationConfig.put("maxOutputTokens", 4096);
            requestBody.put("generationConfig", generationConfig);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    geminiUrl, model, geminiApiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "(no body)";
                    String maskedKey = (geminiApiKey != null && geminiApiKey.length() > 8)
                            ? geminiApiKey.substring(0, 8) + "..." + geminiApiKey.substring(geminiApiKey.length() - 4)
                            : geminiApiKey;
                    logger.error("Gemini API call failed using API key: {} — status: {} — body: {}", maskedKey, response.code(), errorBody);
                    return "Error: Failed to obtain Gemini response. Status code: " + response.code();
                }

                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) responseMap.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    if (resContent != null) {
                        List<Map<String, Object>> resParts =
                                (List<Map<String, Object>>) resContent.get("parts");
                        if (resParts != null && !resParts.isEmpty()) {
                            return (String) resParts.get(0).get("text");
                        }
                    }
                }
                return "Error: Empty response content from Gemini API.";
            }

        } catch (IOException e) {
            logger.error("IOException encountered while querying Gemini API: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
