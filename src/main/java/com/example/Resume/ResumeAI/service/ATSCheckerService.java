package com.example.Resume.ResumeAI.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ATSCheckerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ATSCheckerService.class);
    
    private final GroqAIService groqAIService;
    
    public ATSCheckerService(GroqAIService groqAIService) {
        this.groqAIService = groqAIService;
    }
    
    public Map<String, Object> analyzeResume(String resumeText) {
        Map<String, Object> analysis = new HashMap<>();
        
        try{
            Map<String, Object> basicChecks = performBasicChecks(resumeText);
            analysis.putAll(basicChecks);
            
            Map<String, Object> aiAnalysis = performAIAnalysis(resumeText);
            analysis.putAll(aiAnalysis);
            
            int atsScore = calculateATSScore(analysis);
            analysis.put("atsScore", atsScore);
            analysis.put("feedback", generateFeedback(atsScore, analysis));
            
        }catch(Exception e){
            logger.error("Error analyzing resume", e);
            analysis.put("atsScore", 0);
            analysis.put("feedback", "Error analyzing resume: " + e.getMessage());
        }
        
        return analysis;
    }
    
    private Map<String, Object> performBasicChecks(String text) {
        Map<String, Object> checks = new HashMap<>();
        
        String lower = text.toLowerCase();
        
        checks.put("hasEmail", containsEmail(text));
        checks.put("hasPhone", containsPhone(text));
        checks.put("hasLinks", containsLinks(lower));
        checks.put("hasContactInfo", (boolean)checks.get("hasEmail") || (boolean)checks.get("hasPhone"));
        
        checks.put("hasExperience", lower.contains("experience") || lower.contains("work") || 
                                    lower.contains("employment") || lower.contains("position"));
        checks.put("hasEducation", lower.contains("education") || lower.contains("university") || 
                                   lower.contains("college") || lower.contains("degree"));
        checks.put("hasSkills", lower.contains("skills") || lower.contains("technical") || 
                               lower.contains("proficient"));
        
        checks.put("wordCount", text.split("\\s+").length);
        checks.put("characterCount", text.length());
        
        return checks;
    }
    
    private Map<String, Object> performAIAnalysis(String resumeText) {
        Map<String, Object> aiResults = new HashMap<>();
        
        try{
            Map<String, Object> atsAnalysis = groqAIService.analyzeResumeForATS(
                truncate(resumeText, 5000)
            );
            
            if(atsAnalysis.containsKey("keywords")){
                aiResults.put("keywords", atsAnalysis.get("keywords"));
            } else {
                aiResults.put("keywords", extractKeywords(resumeText));
            }
            
            if(atsAnalysis.containsKey("recommendations")){
                aiResults.put("recommendations", atsAnalysis.get("recommendations"));
            }
            
        }catch(Exception e){
            logger.error("AI analysis failed, using fallback", e);
            aiResults.put("keywords", extractKeywords(resumeText));
            aiResults.put("recommendations", Arrays.asList(
                "Add more quantifiable achievements",
                "Include relevant technical skills"
            ));
        }
        
        return aiResults;
    }
    
    private int calculateATSScore(Map<String, Object> analysis) {
        int score = 50;
        
        if((boolean)analysis.getOrDefault("hasContactInfo", false)) score += 10;
        if((boolean)analysis.getOrDefault("hasEmail", false)) score += 5;
        if((boolean)analysis.getOrDefault("hasPhone", false)) score += 5;
        if((boolean)analysis.getOrDefault("hasExperience", false)) score += 15;
        if((boolean)analysis.getOrDefault("hasEducation", false)) score += 10;
        if((boolean)analysis.getOrDefault("hasSkills", false)) score += 10;
        
        List<?> keywords = (List<?>)analysis.get("keywords");
        if(keywords != null && !keywords.isEmpty()){
            score += Math.min(15, keywords.size() * 2);
        }
        
        int wordCount = (int)analysis.getOrDefault("wordCount", 0);
        if(wordCount >= 300 && wordCount <= 1000) score += 10;
        else if(wordCount < 200) score -= 10;
        
        return Math.min(100, Math.max(0, score));
    }
    
    private String generateFeedback(int score, Map<String, Object> analysis) {
        StringBuilder feedback = new StringBuilder();
        
        if(score >= 80){
            feedback.append("Excellent! Your resume is well-optimized for ATS systems. ");
        } else if(score >= 60){
            feedback.append("Good job! Your resume is ATS-friendly with room for improvement. ");
        } else if(score >= 40){
            feedback.append("Your resume needs work to pass ATS systems effectively. ");
        } else {
            feedback.append("Your resume may struggle with ATS systems and needs significant improvements. ");
        }
        
        if(!(boolean)analysis.getOrDefault("hasContactInfo", false)){
            feedback.append("Add clear contact information. ");
        }
        if(!(boolean)analysis.getOrDefault("hasExperience", false)){
            feedback.append("Include a work experience section. ");
        }
        if(!(boolean)analysis.getOrDefault("hasEducation", false)){
            feedback.append("Add education details. ");
        }
        if(!(boolean)analysis.getOrDefault("hasSkills", false)){
            feedback.append("Include a skills section. ");
        }
        
        List<?> keywords = (List<?>)analysis.get("keywords");
        if(keywords == null || keywords.size() < 5){
            feedback.append("Add more relevant keywords and technical terms. ");
        }
        
        return feedback.toString().trim();
    }
    
    private List<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        String lower = text.toLowerCase();
        
        String[] techKeywords = {
            "java", "python", "javascript", "react", "angular", "vue",
            "spring", "node", "docker", "kubernetes", "aws", "azure",
            "sql", "mongodb", "postgresql", "mysql", "git", "agile",
            "scrum", "ci/cd", "devops", "rest", "api", "microservices",
            "html", "css", "typescript", "c++", "c#", "ruby", "php",
            "swift", "kotlin", "flutter", "android", "ios", "linux"
        };
        
        for(String keyword : techKeywords){
            if(lower.contains(keyword)){
                keywords.add(keyword.substring(0, 1).toUpperCase() + keyword.substring(1));
            }
        }
        
        return new ArrayList<>(keywords);
    }
    
    private boolean containsEmail(String text) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(text);
        return matcher.find();
    }
    
    private boolean containsPhone(String text) {
        Pattern phonePattern = Pattern.compile("\\+?[1-9]\\d{0,3}[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}");
        Matcher matcher = phonePattern.matcher(text);
        return matcher.find();
    }
    
    private boolean containsLinks(String text) {
        return text.contains("linkedin") || text.contains("github") || 
               text.contains("http") || text.contains("www.");
    }
    
    private String truncate(String text, int maxLength) {
        if(text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}