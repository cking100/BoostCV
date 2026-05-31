package com.example.Resume.ResumeAI.repository;

import com.example.Resume.ResumeAI.entity.ResumeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ResumeEmbeddingRepository extends JpaRepository<ResumeEmbedding, Long> {

    @Query("SELECT re FROM ResumeEmbedding re WHERE re.resume.id = :resumeId")
    List<ResumeEmbedding> findByResumeId(@Param("resumeId") Long resumeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ResumeEmbedding re WHERE re.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Long resumeId);
}
