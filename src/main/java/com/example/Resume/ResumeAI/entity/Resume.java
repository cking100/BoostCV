package com.example.Resume.ResumeAI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    private Boolean hasContactInfo;
    private Boolean hasEmail;
    private Boolean hasPhone;
    private Boolean hasLinks;
    private Boolean hasExperience;
    private Boolean hasEducation;
    private Boolean hasSkills;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}