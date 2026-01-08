package com.example.Resume.ResumeAI.repository;

import com.example.Resume.ResumeAI.entity.JobDescription;
import com.example.Resume.ResumeAI.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    
    List<JobDescription> findByUserOrderByCreatedAtDesc(User user);
    
    List<JobDescription> findByUserAndIsActiveOrderByCreatedAtDesc(User user, Boolean isActive);
}