package com.example.Resume.ResumeAI.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ATSCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(ATSCheckerService.class);

    private final GeminiAIService geminiAIService;
    private final GroqAIService   groqAIService;

    public ATSCheckerService(GeminiAIService geminiAIService, GroqAIService groqAIService) {
        this.geminiAIService = geminiAIService;
        this.groqAIService   = groqAIService;
    }

    // ── Public entry point ─────────────────────────────────────────────────────

    public Map<String, Object> analyzeResume(String resumeText) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // Basic regex checks (email, phone, section keywords)
            analysis.putAll(performBasicChecks(resumeText));

            // Deep AI analysis via Gemini
            Map<String, Object> aiAnalysis = performAIAnalysis(resumeText);
            analysis.putAll(aiAnalysis);

            // Determine final ATS score — prefer AI score, then category-weighted, then basic
            int atsScore;
            if (aiAnalysis.containsKey("atsScore")) {
                Object raw = aiAnalysis.get("atsScore");
                atsScore = (raw instanceof Number) ? ((Number) raw).intValue() : 0;
                if (atsScore == 0) {
                    // AI gave 0 — compute from individual category scores
                    atsScore = computeScoreFromCategories(analysis);
                }
            } else {
                // AI returned no atsScore at all — compute from categories + basic checks
                atsScore = computeScoreFromCategories(analysis);
            }

            // Clamp to [0, 100]
            atsScore = Math.min(100, Math.max(0, atsScore));

            analysis.put("atsScore", atsScore);
            analysis.put("feedback", generateFeedback(atsScore));
            logger.info("Resume analysis complete. Final ATS score: {}", atsScore);

        } catch (Exception e) {
            logger.error("Error analyzing resume", e);
            analysis.put("atsScore", 0);
            analysis.put("feedback", "Error analyzing resume: " + e.getMessage());
        }

        return analysis;
    }

    // ── Basic checks (boolean flags for contact / sections) ────────────────────

    private Map<String, Object> performBasicChecks(String text) {
        Map<String, Object> checks = new HashMap<>();
        String lower = text.toLowerCase();

        boolean hasEmail = containsEmail(text);
        boolean hasPhone = containsPhone(text);
        boolean hasLinks = containsLinks(lower);

        checks.put("hasEmail",       hasEmail);
        checks.put("hasPhone",       hasPhone);
        checks.put("hasLinks",       hasLinks);
        checks.put("hasContactInfo", hasEmail || hasPhone);
        checks.put("hasExperience",  lower.contains("experience") || lower.contains("work history") || lower.contains("employment"));
        checks.put("hasEducation",   lower.contains("education") || lower.contains("degree") || lower.contains("university") || lower.contains("college"));
        checks.put("hasSkills",      lower.contains("skills") || lower.contains("technical"));
        checks.put("wordCount",      text.split("\\s+").length);

        return checks;
    }

    // ── Deep AI analysis ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> performAIAnalysis(String resumeText) {
        Map<String, Object> aiResults = new HashMap<>();

        try {
            // Choose AI provider: prefer Gemini, fall back to Groq
            Map<String, Object> atsAnalysis = (groqAIService.isAvailable() && !geminiAIService.isAvailable())
                    ? groqAIService.analyzeResumeForATS(truncate(resumeText, 6000))
                    : geminiAIService.analyzeResumeForATS(truncate(resumeText, 6000));

            if (atsAnalysis == null || atsAnalysis.isEmpty()) {
                logger.warn("AI returned empty analysis map — falling back to basic scoring");
                return aiResults;
            }

            // Store the full breakdown for the frontend
            aiResults.put("atsDetails", atsAnalysis);

            // Extract atsScore — handle Integer, Long, or Double from JSON parse
            Object scoreObj = atsAnalysis.get("atsScore");
            if (scoreObj instanceof Number) {
                aiResults.put("atsScore", ((Number) scoreObj).intValue());
                logger.info("AI atsScore extracted: {}", ((Number) scoreObj).intValue());
            } else {
                logger.warn("atsScore missing or non-numeric in AI response: {}", scoreObj);
                // Don't put anything — computeScoreFromCategories() will handle it
            }

            // Keywords (AI may return them, or we leave empty — frontend handles it)
            if (atsAnalysis.containsKey("keywords")) {
                aiResults.put("keywords", atsAnalysis.get("keywords"));
            } else {
                aiResults.put("keywords", List.of());
            }

            // Build flat recommendations from AI top-level list
            List<String> recs = new ArrayList<>();
            Object recsObj = atsAnalysis.get("recommendations");
            if (recsObj instanceof List) {
                for (Object r : (List<?>) recsObj) {
                    if (r != null && !r.toString().isBlank()) recs.add(r.toString());
                }
            }
            aiResults.put("recommendations", recs);

        } catch (Exception e) {
            logger.error("AI analysis failed: {}", e.getMessage());
            aiResults.put("recommendations", List.of(
                "Add quantified achievements with numbers, percentages, or dollar values",
                "Use strong action verbs: Led, Built, Delivered, Increased, Reduced",
                "Add a professional summary section at the top of your resume",
                "Include relevant industry keywords and certifications",
                "Expand job descriptions to include scope, action, and measurable result"
            ));
        }

        return aiResults;
    }

    // ── Compute score from category sub-scores (fallback when atsScore missing) ─

    @SuppressWarnings("unchecked")
    private int computeScoreFromCategories(Map<String, Object> analysis) {
        // Weighted formula matching the Gemini prompt instructions
        double[][] categoryWeights = {
            {categoryScore(analysis, "impactMetrics"),        0.25},
            {categoryScore(analysis, "actionVerbs"),          0.15},
            {categoryScore(analysis, "keywordDensity"),       0.15},
            {categoryScore(analysis, "experienceDepth"),      0.15},
            {categoryScore(analysis, "contactInfo"),          0.10},
            {categoryScore(analysis, "formatting"),           0.10},
            {categoryScore(analysis, "education"),            0.05},
            {categoryScore(analysis, "professionalPresence"), 0.05},
        };

        double totalWeight = 0;
        double weightedSum = 0;
        for (double[] pair : categoryWeights) {
            if (pair[0] >= 0) { // -1 means category not present
                weightedSum += pair[0] * pair[1];
                totalWeight += pair[1];
            }
        }

        if (totalWeight == 0) {
            // Pure basic-checks fallback if no categories at all
            return computeBasicScore(analysis);
        }

        return (int) Math.round(weightedSum / totalWeight);
    }

    @SuppressWarnings("unchecked")
    private double categoryScore(Map<String, Object> analysis, String key) {
        // Look in atsDetails first
        Object details = analysis.get("atsDetails");
        if (details instanceof Map) {
            Object cat = ((Map<?, ?>) details).get(key);
            if (cat instanceof Map) {
                Object s = ((Map<?, ?>) cat).get("score");
                if (s instanceof Number) return ((Number) s).doubleValue();
            }
        }
        // Also check flat analysis map (when categories are top-level)
        Object cat = analysis.get(key);
        if (cat instanceof Map) {
            Object s = ((Map<?, ?>) cat).get("score");
            if (s instanceof Number) return ((Number) s).doubleValue();
        }
        return -1; // category not available
    }

    private int computeBasicScore(Map<String, Object> analysis) {
        int score = 40;
        if (Boolean.TRUE.equals(analysis.get("hasContactInfo"))) score += 15;
        if (Boolean.TRUE.equals(analysis.get("hasExperience")))  score += 20;
        if (Boolean.TRUE.equals(analysis.get("hasEducation")))   score += 15;
        if (Boolean.TRUE.equals(analysis.get("hasSkills")))      score += 10;
        Object wc = analysis.get("wordCount");
        if (wc instanceof Integer) {
            int words = (Integer) wc;
            if (words >= 300 && words <= 900) score += 10;
            else if (words < 150) score -= 15;
        }
        return Math.min(100, score);
    }

    // ── Feedback string ────────────────────────────────────────────────────────

    private String generateFeedback(int score) {
        if (score >= 85) return "Outstanding! Your resume is highly optimised for ATS systems.";
        if (score >= 70) return "Good job! Your resume is ATS-friendly. A few targeted improvements will push you to the top.";
        if (score >= 55) return "Fair score. Address the issues highlighted below to significantly improve your ATS pass rate.";
        if (score >= 40) return "Your resume needs notable improvements to pass ATS filters effectively.";
        return "Your resume is likely to be filtered out by ATS systems. Prioritise the high-impact fixes listed below.";
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean containsEmail(String text) {
        return Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text).find();
    }

    private boolean containsPhone(String text) {
        return Pattern.compile("(\\+?\\d[\\d\\s\\-().]{6,}\\d)").matcher(text).find();
    }

    private boolean containsLinks(String text) {
        return text.contains("http") || text.contains("linkedin") || text.contains("github");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}