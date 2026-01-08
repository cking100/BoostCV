package com.example.Resume.ResumeAI.controller;

import com.example.Resume.ResumeAI.dto.JobAnalysisRequest;
import com.example.Resume.ResumeAI.dto.JobAnalysisResponse;
import com.example.Resume.ResumeAI.entity.JobDescription;
import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.ResumeAnalysis;
import com.example.Resume.ResumeAI.entity.User;
import com.example.Resume.ResumeAI.repository.JobDescriptionRepository;
import com.example.Resume.ResumeAI.repository.ResumeAnalysisRepository;
import com.example.Resume.ResumeAI.repository.ResumeRepository;
import com.example.Resume.ResumeAI.repository.UserRepository;
import com.example.Resume.ResumeAI.service.AIAnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    
    private final AIAnalysisService aiAnalysisService;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    public AnalysisController(AIAnalysisService aiAnalysisService,
                             ResumeRepository resumeRepository,
                             JobDescriptionRepository jobRepository,
                             ResumeAnalysisRepository analysisRepository,
                             UserRepository userRepository,
                             ObjectMapper objectMapper) {
        this.aiAnalysisService = aiAnalysisService;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("/match-job")
    public ResponseEntity<?> analyzeResumeForJob(@Valid @RequestBody JobAnalysisRequest request,
                                                 Authentication authentication) {
        try {
            // Validate authentication
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            // Validate and fetch resume
            Resume resume = getResumeById(request.getResumeId());
            validateUserOwnsResume(user, resume);
            
            // Handle job description
            JobDescription jobDescription = getOrCreateJobDescription(request, user);
            
            // Perform AI analysis
            Map<String, Object> analysisResult = performAnalysis(resume, jobDescription);
            
            // Save analysis
            ResumeAnalysis analysis = saveAnalysis(resume, jobDescription, analysisResult);
            
            // Convert to response
            JobAnalysisResponse response = convertToResponse(analysis);
            
            return ResponseEntity.ok(response);
            
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (NotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error analyzing resume: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error analyzing resume: " + e.getMessage()));
        }
    }
    
    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<?> getResumeAnalyses(@PathVariable Long resumeId,
                                              Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            Resume resume = getResumeById(resumeId);
            validateUserOwnsResume(user, resume);
            
            List<ResumeAnalysis> analyses = analysisRepository.findByResumeOrderByCreatedAtDesc(resume);
            List<JobAnalysisResponse> responses = analyses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (NotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching analyses: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching analyses: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysis(@PathVariable Long analysisId,
                                        Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            ResumeAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new NotFoundException("Analysis not found"));
            
            validateUserOwnsAnalysis(user, analysis);
            
            return ResponseEntity.ok(convertToResponse(analysis));
            
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (NotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching analysis: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching analysis: " + e.getMessage()));
        }
    }
    
    @PostMapping("/save-job")
    public ResponseEntity<?> saveJobDescription(@Valid @RequestBody JobAnalysisRequest request,
                                               Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            // Validate required fields
            if (request.getJobTitle() == null || request.getJobTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Job title is required"));
            }
            
            JobDescription job = new JobDescription();
            job.setJobTitle(request.getJobTitle());
            job.setCompany(request.getCompany());
            job.setDescription(request.getJobDescription());
            job.setRequirements(request.getRequirements());
            job.setExperienceLevel(request.getExperienceLevel());
            job.setUser(user);
            
            job = jobRepository.save(job);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(job);
            
        } catch (NotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving job: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error saving job: " + e.getMessage()));
        }
    }
    
    @GetMapping("/jobs")
    public ResponseEntity<?> getSavedJobs(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            List<JobDescription> jobs = jobRepository.findByUserOrderByCreatedAtDesc(user);
            
            return ResponseEntity.ok(jobs);
            
        } catch (NotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching jobs: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching jobs: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJob(@PathVariable Long jobId,
                                      Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            String email = authentication.getName();
            User user = getUserByEmail(email);
            
            JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));
            
            validateUserOwnsJob(user, job);
            
            jobRepository.delete(job);
            
            return ResponseEntity.ok().body(Map.of("message", "Job deleted successfully"));
            
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (NotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting job: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error deleting job: " + e.getMessage()));
        }
    }
    
    // Helper methods
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
    
    private Resume getResumeById(Long resumeId) {
        if (resumeId == null) {
            throw new IllegalArgumentException("Resume ID is required");
        }
        return resumeRepository.findById(resumeId)
            .orElseThrow(() -> new NotFoundException("Resume not found"));
    }
    
    private void validateUserOwnsResume(User user, Resume resume) {
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied: You don't have permission to access this resume");
        }
    }
    
    private void validateUserOwnsAnalysis(User user, ResumeAnalysis analysis) {
        if (!analysis.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied: You don't have permission to access this analysis");
        }
    }
    
    private void validateUserOwnsJob(User user, JobDescription job) {
        if (!job.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied: You don't have permission to access this job");
        }
    }
    
    private JobDescription getOrCreateJobDescription(JobAnalysisRequest request, User user) {
        if (request.getJobId() != null) {
            JobDescription job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new NotFoundException("Job not found"));
            validateUserOwnsJob(user, job);
            return job;
        } else if (request.getJobDescription() != null && !request.getJobDescription().trim().isEmpty()) {
            JobDescription jobDescription = new JobDescription();
            jobDescription.setJobTitle(request.getJobTitle() != null ? request.getJobTitle() : "Position");
            jobDescription.setCompany(request.getCompany());
            jobDescription.setDescription(request.getJobDescription());
            jobDescription.setRequirements(request.getRequirements());
            jobDescription.setUser(user);
            return jobRepository.save(jobDescription);
        } else {
            throw new IllegalArgumentException("Either jobId or jobDescription must be provided");
        }
    }
    
    private Map<String, Object> performAnalysis(Resume resume, JobDescription jobDescription) {
        String jobDesc = jobDescription != null ? jobDescription.getDescription() : "";
        String jobTitle = jobDescription != null ? jobDescription.getJobTitle() : "Position";
        String requirements = jobDescription != null ? jobDescription.getRequirements() : "";
        
        if (resume.getExtractedText() == null || resume.getExtractedText().trim().isEmpty()) {
            throw new IllegalArgumentException("Resume has no extracted text to analyze");
        }
        
        return aiAnalysisService.analyzeResumeForJob(
            resume.getExtractedText(),
            jobDesc,
            jobTitle,
            requirements
        );
    }
    
    private ResumeAnalysis saveAnalysis(Resume resume, JobDescription jobDescription, 
                                       Map<String, Object> analysisResult) throws JsonProcessingException {
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(resume);
        analysis.setJobDescription(jobDescription);
        
        // Safely extract and set values with defaults
        analysis.setMatchScore(getIntValue(analysisResult, "matchScore", 0));
        analysis.setAtsScore(resume.getAtsScore() != null ? resume.getAtsScore() : 0);
        analysis.setOverallFeedback(getStringValue(analysisResult, "overallFeedback", "No feedback available"));
        analysis.setImprovedVersion(getStringValue(analysisResult, "improvedVersion", ""));
        
        // Serialize complex objects
        analysis.setMatchedKeywords(serializeToJson(analysisResult.get("matchedKeywords")));
        analysis.setMissingKeywords(serializeToJson(analysisResult.get("missingKeywords")));
        analysis.setGrammarIssues(serializeToJson(analysisResult.get("grammarIssues")));
        analysis.setFormattingIssues(serializeToJson(analysisResult.get("formattingIssues")));
        analysis.setContentSuggestions(serializeToJson(analysisResult.get("contentSuggestions")));
        analysis.setStrengthsWeaknesses(serializeToJson(analysisResult.get("strengthsWeaknesses")));
        
        return analysisRepository.save(analysis);
    }
    
    private Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private String serializeToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return "[]";
        }
        return objectMapper.writeValueAsString(object);
    }
    
    private JobAnalysisResponse convertToResponse(ResumeAnalysis analysis) {
        JobAnalysisResponse response = new JobAnalysisResponse();
        response.setId(analysis.getId());
        response.setResumeId(analysis.getResume().getId());
        response.setResumeFileName(analysis.getResume().getFileName());
        response.setMatchScore(analysis.getMatchScore());
        response.setAtsScore(analysis.getAtsScore());
        response.setOverallFeedback(analysis.getOverallFeedback());
        response.setImprovedVersion(analysis.getImprovedVersion());
        response.setCreatedAt(analysis.getCreatedAt());
        
        if (analysis.getJobDescription() != null) {
            response.setJobTitle(analysis.getJobDescription().getJobTitle());
            response.setCompany(analysis.getJobDescription().getCompany());
        }
        
        // Safe deserialization with error handling
        response.setMatchedKeywords(deserializeList(analysis.getMatchedKeywords()));
        response.setMissingKeywords(deserializeList(analysis.getMissingKeywords()));
        response.setGrammarIssues(deserializeList(analysis.getGrammarIssues()));
        response.setFormattingIssues(deserializeList(analysis.getFormattingIssues()));
        response.setContentSuggestions(deserializeList(analysis.getContentSuggestions()));
        response.setStrengthsWeaknesses(deserializeMap(analysis.getStrengthsWeaknesses()));
        
        return response;
    }
    
    private List<String> deserializeList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private Map<String, Object> deserializeMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
    
    // Custom exception classes
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
    
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}