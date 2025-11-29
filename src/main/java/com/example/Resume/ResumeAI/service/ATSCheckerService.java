package com.example.Resume.ResumeAI.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ATSCheckerService {
    
    private static final Set<String> COMMON_TECH_KEYWORDS = new HashSet<>(Arrays.asList(
        "java", "python", "javascript", "react", "angular", "vue", "spring", "springboot",
        "nodejs", "express", "mysql", "postgresql", "mongodb", "aws", "azure", "docker",
        "kubernetes", "git", "agile", "scrum", "rest", "api", "microservices", "sql",
        "html", "css", "typescript", "c++", "golang", "ruby", "php", "django", "flask"
    ));
    
    public Map<String, Object> analyzeResume(String text) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Extract keywords found
        Set<String> foundKeywords = extractKeywords(text);
        analysis.put("keywords", foundKeywords);
        
        // Check structure elements
        boolean hasContactInfo = checkContactInfo(text);
        boolean hasEmail = checkEmail(text);
        boolean hasPhone = checkPhone(text);
        boolean hasLinks = checkLinks(text);
        boolean hasExperience = checkExperience(text);
        boolean hasEducation = checkEducation(text);
        boolean hasSkills = checkSkills(text);
        
        analysis.put("hasContactInfo", hasContactInfo);
        analysis.put("hasEmail", hasEmail);
        analysis.put("hasPhone", hasPhone);
        analysis.put("hasLinks", hasLinks);
        analysis.put("hasExperience", hasExperience);
        analysis.put("hasEducation", hasEducation);
        analysis.put("hasSkills", hasSkills);
        
        // Calculate ATS score
        int score = calculateATSScore(foundKeywords, hasContactInfo, hasEmail, hasPhone, 
                                      hasLinks, hasExperience, hasEducation, hasSkills);
        analysis.put("atsScore", score);
        
        // Generate feedback
        String feedback = generateFeedback(score, hasContactInfo, hasEmail, hasPhone, 
                                          hasLinks, hasExperience, hasEducation, hasSkills, 
                                          foundKeywords);
        analysis.put("feedback", feedback);
        
        return analysis;
    }
    
    private Set<String> extractKeywords(String text) {
        String lowerText = text.toLowerCase();
        return COMMON_TECH_KEYWORDS.stream()
            .filter(lowerText::contains)
            .collect(Collectors.toSet());
    }
    
    private boolean checkContactInfo(String text) {
        return checkEmail(text) || checkPhone(text);
    }
    
    private boolean checkEmail(String text) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
    
    private boolean checkPhone(String text) {
        Pattern pattern = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
    
    private boolean checkLinks(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("linkedin.com") || 
               lowerText.contains("github.com") || 
               lowerText.contains("portfolio") ||
               lowerText.contains("http://") ||
               lowerText.contains("https://");
    }
    
    private boolean checkExperience(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("experience") || 
               lowerText.contains("work history") ||
               lowerText.contains("employment") ||
               lowerText.contains("professional background");
    }
    
    private boolean checkEducation(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("education") || 
               lowerText.contains("degree") ||
               lowerText.contains("university") ||
               lowerText.contains("college") ||
               lowerText.contains("bachelor") ||
               lowerText.contains("master");
    }
    
    private boolean checkSkills(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("skills") || 
               lowerText.contains("technical skills") ||
               lowerText.contains("competencies");
    }
    
    private int calculateATSScore(Set<String> keywords, boolean hasContactInfo, 
                                  boolean hasEmail, boolean hasPhone, boolean hasLinks,
                                  boolean hasExperience, boolean hasEducation, boolean hasSkills) {
        int score = 0;
        
        // Keywords (40 points max)
        score += Math.min(keywords.size() * 4, 40);
        
        // Contact information (20 points)
        if (hasContactInfo) score += 10;
        if (hasEmail) score += 5;
        if (hasPhone) score += 5;
        
        // Links (10 points)
        if (hasLinks) score += 10;
        
        // Structure (30 points)
        if (hasExperience) score += 15;
        if (hasEducation) score += 10;
        if (hasSkills) score += 5;
        
        return Math.min(score, 100);
    }
    
    private String generateFeedback(int score, boolean hasContactInfo, boolean hasEmail, 
                                   boolean hasPhone, boolean hasLinks, boolean hasExperience, 
                                   boolean hasEducation, boolean hasSkills, Set<String> keywords) {
        StringBuilder feedback = new StringBuilder();
        
        if (score >= 80) {
            feedback.append("Excellent! Your resume is ATS-friendly. ");
        } else if (score >= 60) {
            feedback.append("Good resume, but there's room for improvement. ");
        } else {
            feedback.append("Your resume needs significant improvements for ATS systems. ");
        }
        
        List<String> suggestions = new ArrayList<>();
        
        if (!hasEmail) suggestions.add("Add a valid email address");
        if (!hasPhone) suggestions.add("Include a phone number");
        if (!hasLinks) suggestions.add("Add LinkedIn or GitHub profile links");
        if (!hasExperience) suggestions.add("Include a clear 'Experience' or 'Work History' section");
        if (!hasEducation) suggestions.add("Add an 'Education' section");
        if (!hasSkills) suggestions.add("Create a dedicated 'Skills' section");
        if (keywords.size() < 5) suggestions.add("Include more relevant technical keywords");
        
        if (!suggestions.isEmpty()) {
            feedback.append("\n\nSuggestions:\n");
            for (int i = 0; i < suggestions.size(); i++) {
                feedback.append((i + 1)).append(". ").append(suggestions.get(i)).append("\n");
            }
        }
        
        if (!keywords.isEmpty()) {
            feedback.append("\nKeywords found: ").append(String.join(", ", keywords));
        }
        
        return feedback.toString();
    }
}