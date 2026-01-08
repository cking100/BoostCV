package com.example.Resume.ResumeAI.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobDescription jobDescription;
    
    @Column(nullable = false)
    private Integer matchScore;
    
    @Column(nullable = false)
    private Integer atsScore;
    
    @Column(columnDefinition = "JSON")
    private String matchedKeywords;
    
    @Column(columnDefinition = "JSON")
    private String missingKeywords;
    
    @Column(columnDefinition = "JSON")
    private String grammarIssues;
    
    @Column(columnDefinition = "JSON")
    private String formattingIssues;
    
    @Column(columnDefinition = "JSON")
    private String contentSuggestions;
    
    @Column(columnDefinition = "TEXT")
    private String overallFeedback;
    
    @Column(columnDefinition = "TEXT")
    private String improvedVersion;
    
    @Column(columnDefinition = "JSON")
    private String strengthsWeaknesses;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    
    public ResumeAnalysis() {
    }

    public ResumeAnalysis(Long id, Resume resume, JobDescription jobDescription, Integer matchScore,
                         Integer atsScore, String matchedKeywords, String missingKeywords,
                         String grammarIssues, String formattingIssues, String contentSuggestions,
                         String overallFeedback, String improvedVersion, String strengthsWeaknesses,
                         LocalDateTime createdAt) {
        this.id = id;
        this.resume = resume;
        this.jobDescription = jobDescription;
        this.matchScore = matchScore;
        this.atsScore = atsScore;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.grammarIssues = grammarIssues;
        this.formattingIssues = formattingIssues;
        this.contentSuggestions = contentSuggestions;
        this.overallFeedback = overallFeedback;
        this.improvedVersion = improvedVersion;
        this.strengthsWeaknesses = strengthsWeaknesses;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
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

    public String getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(String matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public String getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(String missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public String getGrammarIssues() {
        return grammarIssues;
    }

    public void setGrammarIssues(String grammarIssues) {
        this.grammarIssues = grammarIssues;
    }

    public String getFormattingIssues() {
        return formattingIssues;
    }

    public void setFormattingIssues(String formattingIssues) {
        this.formattingIssues = formattingIssues;
    }

    public String getContentSuggestions() {
        return contentSuggestions;
    }

    public void setContentSuggestions(String contentSuggestions) {
        this.contentSuggestions = contentSuggestions;
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

    public String getStrengthsWeaknesses() {
        return strengthsWeaknesses;
    }

    public void setStrengthsWeaknesses(String strengthsWeaknesses) {
        this.strengthsWeaknesses = strengthsWeaknesses;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}