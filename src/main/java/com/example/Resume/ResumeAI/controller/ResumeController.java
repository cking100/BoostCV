package com.example.Resume.ResumeAI.controller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.Resume.ResumeAI.dto.ResumeResponse;
import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.User;
import com.example.Resume.ResumeAI.repository.ResumeRepository;
import com.example.Resume.ResumeAI.repository.UserRepository;
import com.example.Resume.ResumeAI.service.ATSCheckerService;
import com.example.Resume.ResumeAI.service.ResumeParserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);
    
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeParserService parserService;
    private final ATSCheckerService atsCheckerService;
    private final ObjectMapper objectMapper;
    private final String uploadDir = "./uploads";
    
    public ResumeController(ResumeRepository resumeRepository,
                           UserRepository userRepository,
                           ResumeParserService parserService,
                           ATSCheckerService atsCheckerService,
                           ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.parserService = parserService;
        this.atsCheckerService = atsCheckerService;
        this.objectMapper = objectMapper;
        
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                         Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            // Get file extension
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
            }
            
            String contentType = file.getContentType();
            
            // Debug logging
            logger.info("=== File Upload Debug Info ===");
            logger.info("Original Filename: {}", originalFilename);
            logger.info("Content-Type: {}", contentType);
            logger.info("File Extension: {}", fileExtension);
            logger.info("File Size: {} bytes", file.getSize());
            
            // Validate file extension first (more reliable)
            if (!fileExtension.equals("pdf") && !fileExtension.equals("docx")) {
                logger.error("Invalid file extension: {}", fileExtension);
                return ResponseEntity.badRequest().body("Only PDF and DOCX files are allowed. File extension: " + fileExtension);
            }
            
            // Validate content type (more lenient)
            if (contentType != null && !isValidFileType(contentType)) {
                logger.warn("Content type validation failed for: {}", contentType);
                // Only reject if extension is also invalid
                if (!fileExtension.equals("pdf") && !fileExtension.equals("docx")) {
                    return ResponseEntity.badRequest().body("Unsupported file type: " + contentType);
                }
                logger.info("Allowing upload based on valid file extension despite content type: {}", contentType);
            }
            
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File saved to: {}", filePath.toString());
            
            // Use file extension to determine parser if content type is unreliable
            String effectiveContentType = contentType;
            if (contentType == null || contentType.isEmpty()) {
                if (fileExtension.equals("pdf")) {
                    effectiveContentType = "application/pdf";
                } else if (fileExtension.equals("docx")) {
                    effectiveContentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
                logger.info("Using effective content type based on extension: {}", effectiveContentType);
            }
            
            String extractedText = parserService.parseResume(filePath.toString(), effectiveContentType);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                logger.error("Failed to extract text from resume");
                return ResponseEntity.internalServerError().body("Failed to extract text from resume");
            }
            
            logger.info("Extracted text length: {} characters", extractedText.length());
            
            Map<String, Object> analysis = atsCheckerService.analyzeResume(extractedText);
            
            logger.info("Analysis completed. ATS Score: {}", analysis.get("atsScore"));
            
            Resume resume = new Resume();
            resume.setFileName(originalFilename);
            resume.setFilePath(filePath.toString());
            resume.setExtractedText(extractedText);
            resume.setAtsScore((Integer) analysis.get("atsScore"));
            resume.setFeedback((String) analysis.get("feedback"));
            resume.setKeywords(objectMapper.writeValueAsString(analysis.get("keywords")));
            resume.setHasContactInfo((Boolean) analysis.get("hasContactInfo"));
            resume.setHasEmail((Boolean) analysis.get("hasEmail"));
            resume.setHasPhone((Boolean) analysis.get("hasPhone"));
            resume.setHasLinks((Boolean) analysis.get("hasLinks"));
            resume.setHasExperience((Boolean) analysis.get("hasExperience"));
            resume.setHasEducation((Boolean) analysis.get("hasEducation"));
            resume.setHasSkills((Boolean) analysis.get("hasSkills"));
            resume.setUser(user);
            
            resumeRepository.save(resume);
            
            logger.info("Resume saved successfully with ID: {}", resume.getId());
            
            return ResponseEntity.ok(convertToResponse(resume));
            
        } catch (Exception e) {
            logger.error("Error processing resume upload", e);
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing resume: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getUserResumes(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Resume> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user);
        List<ResumeResponse> responses = resumes.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        if (!resume.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        return ResponseEntity.ok(convertToResponse(resume));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        if (!resume.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        try {
            Files.deleteIfExists(Paths.get(resume.getFilePath()));
        } catch (IOException e) {
            logger.error("Error deleting file: {}", resume.getFilePath(), e);
        }
        
        resumeRepository.delete(resume);
        
        return ResponseEntity.ok().body("Resume deleted successfully");
    }
    
    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        // Normalize content type (trim and lowercase)
        String normalized = contentType.trim().toLowerCase();
        
        // Check for PDF
        if (normalized.equals("application/pdf") || 
            normalized.startsWith("application/pdf;")) {
            return true;
        }
        
        // Check for DOCX
        if (normalized.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            normalized.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document;")) {
            return true;
        }
        
        // Check for older DOC format (just in case)
        if (normalized.equals("application/msword") ||
            normalized.startsWith("application/msword;")) {
            return true;
        }
        
        return false;
    }
    
    private ResumeResponse convertToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setFileName(resume.getFileName());
        response.setAtsScore(resume.getAtsScore());
        response.setFeedback(resume.getFeedback());
        response.setCreatedAt(resume.getCreatedAt());
        response.setHasContactInfo(resume.getHasContactInfo());
        response.setHasEmail(resume.getHasEmail());
        response.setHasPhone(resume.getHasPhone());
        response.setHasLinks(resume.getHasLinks());
        response.setHasExperience(resume.getHasExperience());
        response.setHasEducation(resume.getHasEducation());
        response.setHasSkills(resume.getHasSkills());
        
        try {
            response.setKeywords(objectMapper.readValue(resume.getKeywords(), List.class));
        } catch (Exception e) {
            response.setKeywords(List.of());
        }
        
        return response;
    }
}