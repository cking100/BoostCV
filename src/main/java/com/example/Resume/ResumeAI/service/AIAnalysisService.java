package com.example.Resume.ResumeAI.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);
    
    private final GroqAIService groqAIService;
    private final ObjectMapper objectMapper;
    
    public AIAnalysisService(GroqAIService groqAIService, ObjectMapper objectMapper) {
        this.groqAIService = groqAIService;
        this.objectMapper = objectMapper;
    }
    
    public Map<String, Object> analyzeResumeForJob(String resumeText, String jobDescription, 
                                                   String jobTitle, String requirements) {
        Map<String, Object> result = new HashMap<>();
        
        try{
            String combinedJob = buildJobContext(jobDescription, jobTitle, requirements);
            
            result.put("matchScore", calculateMatchScore(resumeText, combinedJob));
            result.put("overallFeedback", generateOverallFeedback(resumeText, combinedJob));
            result.put("matchedKeywords", extractMatchedKeywords(resumeText, combinedJob));
            result.put("missingKeywords", extractMissingKeywords(resumeText, combinedJob));
            result.put("grammarIssues", checkGrammar(resumeText));
            result.put("formattingIssues", checkFormatting(resumeText));
            result.put("contentSuggestions", generateSuggestions(resumeText, combinedJob));
            result.put("strengthsWeaknesses", analyzeStrengthsWeaknesses(resumeText, combinedJob));
            result.put("improvedVersion", generateImprovedVersion(resumeText, combinedJob));
            
        }catch(Exception e){
            logger.error("Error in AI analysis", e);
            result.put("error", e.getMessage());
            result.put("matchScore", 0);
            result.put("overallFeedback", "Analysis failed: " + e.getMessage());
        }
        
        return result;
    }
    
    private String buildJobContext(String description, String title, String requirements) {
        StringBuilder context = new StringBuilder();
        if(title != null && !title.isEmpty()){
            context.append("Job Title: ").append(title).append("\n\n");
        }
        if(description != null && !description.isEmpty()){
            context.append("Description: ").append(description).append("\n\n");
        }
        if(requirements != null && !requirements.isEmpty()){
            context.append("Requirements: ").append(requirements);
        }
        return context.toString();
    }
    
    private int calculateMatchScore(String resume, String job) {
        try{
            String prompt = String.format(
                "Analyze how well this resume matches the job. Give a match score from 0-100.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\n" +
                "Respond with ONLY a number between 0-100:",
                truncate(job, 2000), truncate(resume, 3000)
            );
            
            String response = groqAIService.generateContentSuggestions(resume, job);
            String scoreStr = response.replaceAll("[^0-9]", "");
            if(!scoreStr.isEmpty()){
                int score = Integer.parseInt(scoreStr.substring(0, Math.min(scoreStr.length(), 3)));
                return Math.min(100, Math.max(0, score));
            }
        }catch(Exception e){
            logger.error("Error calculating match score", e);
        }
        return 65;
    }
    
    private String generateOverallFeedback(String resume, String job) {
        try{
            return groqAIService.generateResumeImprovement(truncate(resume, 3000), truncate(job, 2000));
        }catch(Exception e){
            logger.error("Error generating feedback", e);
            return "Unable to generate feedback at this time.";
        }
    }
    
    private List<String> extractMatchedKeywords(String resume, String job) {
        try{
            String prompt = String.format(
                "List technical keywords and skills that appear in BOTH the job and resume. " +
                "Return ONLY a comma-separated list, no other text.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\nMatched keywords:",
                truncate(job, 2000), truncate(resume, 3000)
            );
            
            String response = groqAIService.analyzeMissingKeywords(resume, job);
            return parseKeywordList(response);
        }catch(Exception e){
            logger.error("Error extracting matched keywords", e);
            return Arrays.asList("Java", "Spring Boot", "React");
        }
    }
    
    private List<String> extractMissingKeywords(String resume, String job) {
        try{
            String response = groqAIService.analyzeMissingKeywords(truncate(resume, 3000), truncate(job, 2000));
            return parseKeywordList(response);
        }catch(Exception e){
            logger.error("Error extracting missing keywords", e);
            return Arrays.asList("Docker", "Kubernetes", "CI/CD");
        }
    }
    
    private List<String> checkGrammar(String resume) {
        try{
            String response = groqAIService.checkGrammar(truncate(resume, 4000));
            return parseIssuesList(response);
        }catch(Exception e){
            logger.error("Error checking grammar", e);
            return new ArrayList<>();
        }
    }
    
    private List<String> checkFormatting(String resume) {
        List<String> issues = new ArrayList<>();
        
        if(resume.length() < 300){
            issues.add("Resume seems too short");
        }
        if(resume.length() > 8000){
            issues.add("Resume might be too long");
        }
        if(!resume.toLowerCase().contains("experience") && !resume.toLowerCase().contains("work")){
            issues.add("No clear experience section found");
        }
        if(!resume.toLowerCase().contains("education")){
            issues.add("No education section found");
        }
        
        return issues;
    }
    
    private List<String> generateSuggestions(String resume, String job) {
        try{
            String response = groqAIService.generateContentSuggestions(truncate(resume, 3000), truncate(job, 2000));
            return parseIssuesList(response);
        }catch(Exception e){
            logger.error("Error generating suggestions", e);
            return Arrays.asList(
                "Add more quantifiable achievements",
                "Include relevant technical skills",
                "Tailor experience to job requirements"
            );
        }
    }
    
    private Map<String, Object> analyzeStrengthsWeaknesses(String resume, String job) {
        Map<String, Object> analysis = new HashMap<>();
        
        try{
            String prompt = String.format(
                "Analyze this resume for the job. List 3 strengths and 3 weaknesses.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\n" +
                "Format:\nStrengths:\n1. \n2. \n3. \n\nWeaknesses:\n1. \n2. \n3. ",
                truncate(job, 2000), truncate(resume, 3000)
            );
            
            String response = groqAIService.generateResumeImprovement(resume, job);
            
            List<String> strengths = extractSection(response, "strength");
            List<String> weaknesses = extractSection(response, "weakness");
            
            analysis.put("strengths", strengths.isEmpty() ? 
                Arrays.asList("Good technical background", "Clear formatting", "Relevant experience") : strengths);
            analysis.put("weaknesses", weaknesses.isEmpty() ? 
                Arrays.asList("Could add more metrics", "Missing some key skills") : weaknesses);
                
        }catch(Exception e){
            logger.error("Error analyzing strengths/weaknesses", e);
            analysis.put("strengths", Arrays.asList("Technical skills present"));
            analysis.put("weaknesses", Arrays.asList("Could be more detailed"));
        }
        
        return analysis;
    }
    
    private String generateImprovedVersion(String resume, String job) {
        try{
            String prompt = String.format(
                "Suggest 3-5 specific improvements to this resume for the job. Be concise.\n\n" +
                "Job:\n%s\n\nResume:\n%s\n\nImprovement suggestions:",
                truncate(job, 2000), truncate(resume, 3000)
            );
            
            return groqAIService.generateContentSuggestions(resume, job);
        }catch(Exception e){
            logger.error("Error generating improved version", e);
            return "Add more specific achievements and quantify your impact.";
        }
    }
    
    private List<String> parseKeywordList(String response) {
        if(response == null || response.trim().isEmpty()){
            return new ArrayList<>();
        }
        
        String[] parts = response.split("[,\n]");
        List<String> keywords = new ArrayList<>();
        for(String part : parts){
            String cleaned = part.trim().replaceAll("^[0-9.\\-*]+\\s*", "");
            if(!cleaned.isEmpty() && cleaned.length() > 2){
                keywords.add(cleaned);
            }
        }
        return keywords;
    }
    
    private List<String> parseIssuesList(String response) {
        if(response == null || response.trim().isEmpty()){
            return new ArrayList<>();
        }
        
        List<String> issues = new ArrayList<>();
        String[] lines = response.split("\n");
        for(String line : lines){
            String cleaned = line.trim().replaceAll("^[0-9.\\-*]+\\s*", "");
            if(!cleaned.isEmpty() && cleaned.length() > 10){
                issues.add(cleaned);
            }
        }
        return issues.isEmpty() ? Arrays.asList("No major issues found") : issues;
    }
    
    private List<String> extractSection(String text, String sectionName) {
        List<String> items = new ArrayList<>();
        String lower = text.toLowerCase();
        int startIdx = lower.indexOf(sectionName);
        
        if(startIdx == -1) return items;
        
        String section = text.substring(startIdx);
        String[] lines = section.split("\n");
        
        for(int i = 1; i < Math.min(lines.length, 5); i++){
            String line = lines[i].trim().replaceAll("^[0-9.\\-*]+\\s*", "");
            if(!line.isEmpty() && line.length() > 10){
                items.add(line);
                if(items.size() >= 3) break;
            }
        }
        
        return items;
    }
    
    private String truncate(String text, int maxLength) {
        if(text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}