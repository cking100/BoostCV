package com.example.Resume.ResumeAI.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeResponse {
    
    private Long id;
    
    private String fileName;
    
    private String filePath;
    
    private Integer atsScore;
    
    private String feedback;
    
    private List<String> keywords;
    
    private List<String> missingKeywords;
    
    private Boolean hasContactInfo;
    
    private Boolean hasEmail;
    
    private Boolean hasPhone;
    
    private Boolean hasLinks;
    
    private Boolean hasExperience;
    
    private Boolean hasEducation;
    
    private Boolean hasSkills;
    
    private LocalDateTime createdAt;

    // Constructors
    
    public ResumeResponse() {
    }

    public ResumeResponse(Long id, String fileName, String filePath, Integer atsScore,
                         String feedback, List<String> keywords, List<String> missingKeywords,
                         Boolean hasContactInfo, Boolean hasEmail, Boolean hasPhone, 
                         Boolean hasLinks, Boolean hasExperience, Boolean hasEducation, 
                         Boolean hasSkills, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.atsScore = atsScore;
        this.feedback = feedback;
        this.keywords = keywords;
        this.missingKeywords = missingKeywords;
        this.hasContactInfo = hasContactInfo;
        this.hasEmail = hasEmail;
        this.hasPhone = hasPhone;
        this.hasLinks = hasLinks;
        this.hasExperience = hasExperience;
        this.hasEducation = hasEducation;
        this.hasSkills = hasSkills;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public Boolean getHasContactInfo() {
        return hasContactInfo;
    }

    public void setHasContactInfo(Boolean hasContactInfo) {
        this.hasContactInfo = hasContactInfo;
    }

    public Boolean getHasEmail() {
        return hasEmail;
    }

    public void setHasEmail(Boolean hasEmail) {
        this.hasEmail = hasEmail;
    }

    public Boolean getHasPhone() {
        return hasPhone;
    }

    public void setHasPhone(Boolean hasPhone) {
        this.hasPhone = hasPhone;
    }

    public Boolean getHasLinks() {
        return hasLinks;
    }

    public void setHasLinks(Boolean hasLinks) {
        this.hasLinks = hasLinks;
    }

    public Boolean getHasExperience() {
        return hasExperience;
    }

    public void setHasExperience(Boolean hasExperience) {
        this.hasExperience = hasExperience;
    }

    public Boolean getHasEducation() {
        return hasEducation;
    }

    public void setHasEducation(Boolean hasEducation) {
        this.hasEducation = hasEducation;
    }

    public Boolean getHasSkills() {
        return hasSkills;
    }

    public void setHasSkills(Boolean hasSkills) {
        this.hasSkills = hasSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}