package com.example.Resume.ResumeAI.dto;

import java.time.LocalDateTime;

public class JobDescriptionDto {
    
    private Long id;
    
    private String jobTitle;
    
    private String company;
    
    private String description;
    
    private String requirements;
    
    private String experienceLevel;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;

    // Constructors
    
    public JobDescriptionDto() {
    }

    public JobDescriptionDto(Long id, String jobTitle, String company, String description,
                            String requirements, String experienceLevel, Boolean isActive,
                            LocalDateTime createdAt) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.company = company;
        this.description = description;
        this.requirements = requirements;
        this.experienceLevel = experienceLevel;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}