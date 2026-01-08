package com.example.Resume.ResumeAI.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class JobAnalysisResponse {
    
    private Long id;
    
    private Long resumeId;
    
    private String resumeFileName;
    
    private String jobTitle;
    
    private String company;
    
    private Integer matchScore;
    
    private Integer atsScore;
    
    private List<String> matchedKeywords;
    
    private List<String> missingKeywords;
    
    private List<String> grammarIssues;
    
    private List<String> formattingIssues;
    
    private List<String> contentSuggestions;
    
    private Map<String, Object> strengthsWeaknesses;
    
    private String overallFeedback;
    
    private String improvedVersion;
    
    private LocalDateTime createdAt;

    // Constructors
    
    public JobAnalysisResponse() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public List<String> getGrammarIssues() {
        return grammarIssues;
    }

    public void setGrammarIssues(List<String> grammarIssues) {
        this.grammarIssues = grammarIssues;
    }

    public List<String> getFormattingIssues() {
        return formattingIssues;
    }

    public void setFormattingIssues(List<String> formattingIssues) {
        this.formattingIssues = formattingIssues;
    }

    public List<String> getContentSuggestions() {
        return contentSuggestions;
    }

    public void setContentSuggestions(List<String> contentSuggestions) {
        this.contentSuggestions = contentSuggestions;
    }

    public Map<String, Object> getStrengthsWeaknesses() {
        return strengthsWeaknesses;
    }

    public void setStrengthsWeaknesses(Map<String, Object> strengthsWeaknesses) {
        this.strengthsWeaknesses = strengthsWeaknesses;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getImprovedVersion() {
        return improvedVersion;
    }

    public void setImprovedVersion(String improvedVersion) {
        this.improvedVersion = improvedVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}