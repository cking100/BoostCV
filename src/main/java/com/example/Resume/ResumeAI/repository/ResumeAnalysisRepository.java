package com.example.Resume.ResumeAI.repository;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    
    List<ResumeAnalysis> findByResumeOrderByCreatedAtDesc(Resume resume);
    
    List<ResumeAnalysis> findByResume(Resume resume);
}