package com.example.Resume.ResumeAI.repository;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
    
    List<Resume> findByUser(User user);
}