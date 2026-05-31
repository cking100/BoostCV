package com.example.Resume.ResumeAI.service;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.ResumeEmbedding;
import com.example.Resume.ResumeAI.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private final GeminiAIService geminiAIService;
    private final VectorStoreService vectorStoreService;
    private final ResumeRepository resumeRepository;

    public AIService(GeminiAIService geminiAIService, 
                     VectorStoreService vectorStoreService,
                     ResumeRepository resumeRepository) {
        this.geminiAIService = geminiAIService;
        this.vectorStoreService = vectorStoreService;
        this.resumeRepository = resumeRepository;
    }

    /**
     * Executes a Retrieval-Augmented Generation (RAG) conversational response.
     * Grabs only the semantically relevant sections of the specific resume matching the user's query
     * and uses the LLM to deliver a highly context-aware coaching response.
     */
    public String getRAGCoachingResponse(Long resumeId, String userQuery) {
        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
        if (resumeOpt.isEmpty()) {
            return "Error: Resume with ID " + resumeId + " not found.";
        }

        Resume resume = resumeOpt.get();
        logger.info("Performing RAG Coaching Query for Resume ID: {}, Query: '{}'", resumeId, userQuery);

        // 1. Retrieve the top 3 semantically similar segments of the resume
        List<ResumeEmbedding> matchingChunks = vectorStoreService.searchSimilarForResume(resumeId, userQuery, 3);

        // 2. Build the context block from retrieved chunks
        StringBuilder contextBuilder = new StringBuilder();
        if (matchingChunks.isEmpty()) {
            // Fall back to full text if vector matching yields nothing
            contextBuilder.append("Candidate Full Profile Context:\n")
                          .append(truncate(resume.getExtractedText(), 2000));
        } else {
            contextBuilder.append("Retrieved Candidate Profile Context:\n");
            for (ResumeEmbedding chunk : matchingChunks) {
                contextBuilder.append("--- [Section: ").append(chunk.getSectionName()).append("] ---\n")
                              .append(chunk.getContent()).append("\n\n");
            }
        }

        // 3. Assemble the RAG prompt
        String ragPrompt = String.format(
            "Based ON the provided resume segments, answer the candidate's career query. " +
            "Reference specific experiences or skills from the context when explaining your suggestions. " +
            "If the provided resume sections are insufficient to fully answer, advise them but prioritize using their profile.\n\n" +
            "--- CONTEXT START ---\n" +
            "%s" +
            "--- CONTEXT END ---\n\n" +
            "Candidate Query: %s\n\n" +
            "Actionable Advice:",
            contextBuilder.toString(), userQuery
        );

        // 4. Generate coaching feedback via the Gemini LLM
        return geminiAIService.generateContentSuggestions(
            ragPrompt, 
            "You are a professional executive career development coach and ATS optimization specialist."
        );
    }

    /**
     * Smart Sourcing / Candidate Recommendation RAG pipeline.
     * Given a job description, retrieves the overall most similar resumes/chunks in the system,
     * and compiles matching highlights.
     */
    public List<Map<String, Object>> findMatchingCandidates(String jobDescription, int maxCandidates) {
        logger.info("Matching candidates for job requirements: '{}'", truncate(jobDescription, 50));

        // 1. Fetch overall matching vector embeddings
        List<ResumeEmbedding> similarChunks = vectorStoreService.searchSimilar(jobDescription, maxCandidates * 2);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<Long> addedResumeIds = new ArrayList<>();

        for (ResumeEmbedding chunk : similarChunks) {
            Resume resume = chunk.getResume();
            if (resume == null || addedResumeIds.contains(resume.getId())) {
                continue;
            }

            addedResumeIds.add(resume.getId());

            Map<String, Object> candidateMatch = new HashMap<>();
            candidateMatch.put("resumeId", resume.getId());
            candidateMatch.put("fileName", resume.getFileName());
            candidateMatch.put("atsScore", resume.getAtsScore());
            candidateMatch.put("relevantSection", chunk.getSectionName());
            candidateMatch.put("snippet", truncate(chunk.getContent(), 250));

            // Generate a rapid explanation of why this chunk matches the job requirements
            String explanationPrompt = String.format(
                "In 1-2 sentence highlights, explain why this resume section matches the target job role.\n\n" +
                "Target Job Role/Description:\n%s\n\n" +
                "Resume Section [%s]:\n%s\n\n" +
                "Matching Highlight:",
                truncate(jobDescription, 500), chunk.getSectionName(), truncate(chunk.getContent(), 600)
            );

            try {
                String matchHighlight = geminiAIService.generateContentSuggestions(explanationPrompt, "You are an HR sourcer and technical recruiter.");
                candidateMatch.put("matchReason", matchHighlight.trim());
            } catch (Exception e) {
                candidateMatch.put("matchReason", "Matches target keywords in " + chunk.getSectionName());
            }

            recommendations.add(candidateMatch);
            if (recommendations.size() >= maxCandidates) {
                break;
            }
        }

        return recommendations;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}