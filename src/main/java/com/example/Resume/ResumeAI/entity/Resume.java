package com.example.Resume.ResumeAI.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    
    @Column(nullable = false)
    private Integer atsScore;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(columnDefinition = "JSON")
    private String keywords;
    
    @Column(columnDefinition = "JSON")
    private String missingKeywords;

    @Column(columnDefinition = "TEXT")
    private String atsDetails;
    
    private Boolean hasContactInfo;
    private Boolean hasEmail;
    private Boolean hasPhone;
    private Boolean hasLinks;
    private Boolean hasExperience;
    private Boolean hasEducation;
    private Boolean hasSkills;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResumeAnalysis> analyses = new ArrayList<>();
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    
    public Resume() {
    }

    public Resume(Long id, String fileName, String filePath, String extractedText, Integer atsScore,
                 String feedback, String keywords, String missingKeywords, Boolean hasContactInfo,
                 Boolean hasEmail, Boolean hasPhone, Boolean hasLinks, Boolean hasExperience,
                 Boolean hasEducation, Boolean hasSkills, User user, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.extractedText = extractedText;
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
        this.user = user;
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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
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

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(String missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public String getAtsDetails() {
        return atsDetails;
    }

    public void setAtsDetails(String atsDetails) {
        this.atsDetails = atsDetails;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ResumeAnalysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<ResumeAnalysis> analyses) {
        this.analyses = analyses;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}