package com.example.Resume.ResumeAI.dto;

import jakarta.validation.constraints.NotNull;

public class JobAnalysisRequest {
    
    @NotNull(message = "Resume ID is required")
    private Long resumeId;
    
    private Long jobId;
    
    private String jobTitle;
    
    private String company;
    
    private String jobDescription;
    
    private String requirements;
    
    private String experienceLevel;

    // Constructors
    
    public JobAnalysisRequest() {
    }

    // Getters and Setters
    
    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
}