package com.example.Resume.ResumeAI.dto;

public class ResumeUploadResponse {
    
    private Long resumeId;
    
    private String fileName;
    
    private String message;
    
    private Integer atsScore;

    // Constructors
    
    public ResumeUploadResponse() {
    }

    public ResumeUploadResponse(Long resumeId, String fileName, String message, Integer atsScore) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.message = message;
        this.atsScore = atsScore;
    }

    // Getters and Setters
    
    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }
}