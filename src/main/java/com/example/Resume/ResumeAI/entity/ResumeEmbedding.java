package com.example.Resume.ResumeAI.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "resume_embeddings")
public class ResumeEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String sectionName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String embeddingString;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ResumeEmbedding() {
    }

    public ResumeEmbedding(Resume resume, String content, String sectionName, String embeddingString) {
        this.resume = resume;
        this.content = content;
        this.sectionName = sectionName;
        this.embeddingString = embeddingString;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getEmbeddingString() {
        return embeddingString;
    }

    public void setEmbeddingString(String embeddingString) {
        this.embeddingString = embeddingString;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
