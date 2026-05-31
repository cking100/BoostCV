package com.example.Resume.ResumeAI.controller;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.repository.ResumeRepository;
import com.example.Resume.ResumeAI.service.AIService;
import com.example.Resume.ResumeAI.service.GeminiAIService;
import com.example.Resume.ResumeAI.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;
    private final VectorStoreService vectorStoreService;
    private final ResumeRepository resumeRepository;
    private final GeminiAIService geminiAIService;

    public AIController(AIService aiService,
                        VectorStoreService vectorStoreService,
                        ResumeRepository resumeRepository,
                        GeminiAIService geminiAIService) {
        this.aiService = aiService;
        this.vectorStoreService = vectorStoreService;
        this.resumeRepository = resumeRepository;
        this.geminiAIService = geminiAIService;
    }

    // ─────────────────────────────────────────────────────────────────
    //  EXISTING ENDPOINTS
    // ─────────────────────────────────────────────────────────────────

    /**
     * Explicit endpoint to parse, chunk, and index a specific resume's embeddings.
     */
    @PostMapping("/index/{resumeId}")
    public ResponseEntity<?> indexResume(@PathVariable Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID " + resumeId));

        vectorStoreService.indexResume(resume);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Resume successfully indexed in the vector store.");
        response.put("resumeId", resumeId);
        return ResponseEntity.ok(response);
    }

    /**
     * RAG Coaching query: Answers career questions grounded in the user's resume chunks.
     */
    @PostMapping("/coaching")
    public ResponseEntity<?> getCoaching(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("resumeId") || !payload.containsKey("query")) {
            return ResponseEntity.badRequest().body("Payload must contain both 'resumeId' and 'query'");
        }

        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        String query = payload.get("query").toString();

        String advice = aiService.getRAGCoachingResponse(resumeId, query);

        Map<String, Object> response = new HashMap<>();
        response.put("advice", advice);
        return ResponseEntity.ok(response);
    }

    /**
     * Smart Candidate Matching: Finds top resume sections matching job requirements.
     */
    @PostMapping("/match-candidates")
    public ResponseEntity<?> matchCandidates(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("jobDescription")) {
            return ResponseEntity.badRequest().body("Payload must contain 'jobDescription'");
        }

        String jobDescription = payload.get("jobDescription").toString();
        int limit = payload.containsKey("limit")
                ? Integer.parseInt(payload.get("limit").toString()) : 5;

        List<Map<String, Object>> matches = aiService.findMatchingCandidates(jobDescription, limit);
        return ResponseEntity.ok(matches);
    }

    // ─────────────────────────────────────────────────────────────────
    //  NEW GEN AI ENDPOINTS
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cover Letter Generator — generates a tailored cover letter for a job application.
     * Body: { resumeId, jobTitle, company, jobDescription }
     */
    @PostMapping("/cover-letter")
    public ResponseEntity<?> generateCoverLetter(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("resumeId")) {
            return ResponseEntity.badRequest().body("Payload must contain 'resumeId'");
        }

        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID " + resumeId));

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            return ResponseEntity.badRequest().body("Resume has no extracted text to work with.");
        }

        String jobTitle       = payload.getOrDefault("jobTitle", "Software Engineer").toString();
        String company        = payload.getOrDefault("company", "").toString();
        String jobDescription = payload.getOrDefault("jobDescription", "").toString();

        try {
            String coverLetter = geminiAIService.generateCoverLetter(
                    resume.getExtractedText(), jobTitle, company, jobDescription);

            Map<String, Object> response = new HashMap<>();
            response.put("coverLetter", coverLetter);
            response.put("resumeId", resumeId);
            response.put("jobTitle", jobTitle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating cover letter for resume {}", resumeId, e);
            return ResponseEntity.internalServerError()
                    .body("Error generating cover letter: " + e.getMessage());
        }
    }

    /**
     * Interview Coach — generates 10 Q&A pairs tailored to the resume + job.
     * Body: { resumeId, jobDescription }
     */
    @PostMapping("/interview-coach")
    public ResponseEntity<?> getInterviewCoach(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("resumeId")) {
            return ResponseEntity.badRequest().body("Payload must contain 'resumeId'");
        }

        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID " + resumeId));

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            return ResponseEntity.badRequest().body("Resume has no extracted text to work with.");
        }

        String jobDescription = payload.getOrDefault("jobDescription", "").toString();

        try {
            String questions = geminiAIService.generateInterviewQuestions(
                    resume.getExtractedText(), jobDescription);

            Map<String, Object> response = new HashMap<>();
            response.put("questionsAndAnswers", questions);
            response.put("resumeId", resumeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating interview questions for resume {}", resumeId, e);
            return ResponseEntity.internalServerError()
                    .body("Error generating interview questions: " + e.getMessage());
        }
    }

    /**
     * Career Path Advisor — generates 3 personalised career paths with 90-day action plans.
     * Body: { resumeId }
     */
    @PostMapping("/career-paths")
    public ResponseEntity<?> getCareerPaths(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("resumeId")) {
            return ResponseEntity.badRequest().body("Payload must contain 'resumeId'");
        }

        Long resumeId = Long.valueOf(payload.get("resumeId").toString());
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID " + resumeId));

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            return ResponseEntity.badRequest().body("Resume has no extracted text to work with.");
        }

        try {
            String careerPaths = geminiAIService.suggestCareerPaths(resume.getExtractedText());

            Map<String, Object> response = new HashMap<>();
            response.put("careerPaths", careerPaths);
            response.put("resumeId", resumeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating career paths for resume {}", resumeId, e);
            return ResponseEntity.internalServerError()
                    .body("Error generating career paths: " + e.getMessage());
        }
    }
}