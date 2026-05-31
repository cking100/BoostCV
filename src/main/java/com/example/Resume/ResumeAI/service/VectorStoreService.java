package com.example.Resume.ResumeAI.service;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.ResumeEmbedding;
import com.example.Resume.ResumeAI.repository.ResumeEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final EmbeddingService embeddingService;
    private final ResumeEmbeddingRepository resumeEmbeddingRepository;

    public VectorStoreService(EmbeddingService embeddingService, 
                              ResumeEmbeddingRepository resumeEmbeddingRepository) {
        this.embeddingService = embeddingService;
        this.resumeEmbeddingRepository = resumeEmbeddingRepository;
    }

    /**
     * Processes and indexes a resume by splitting it into smart chunks, 
     * generating vector embeddings, and persisting them in PostgreSQL.
     */
    @Transactional
    public void indexResume(Resume resume) {
        if (resume == null || resume.getExtractedText() == null || resume.getExtractedText().trim().isEmpty()) {
            logger.warn("Cannot index empty resume: {}", resume != null ? resume.getId() : "null");
            return;
        }

        logger.info("Starting indexing for resume ID: {}", resume.getId());

        // 1. Delete existing embeddings for this resume to prevent duplicates
        resumeEmbeddingRepository.deleteByResumeId(resume.getId());

        // 2. Perform smart segmentation / chunking
        List<ResumeChunk> chunks = chunkResumeText(resume.getExtractedText());

        // Add a chunk for the full resume text as well to ensure holistic matching
        chunks.add(new ResumeChunk(resume.getExtractedText(), "Full Profile"));

        // 3. Generate embeddings and save chunks
        List<ResumeEmbedding> embeddingsToSave = new ArrayList<>();
        for (ResumeChunk chunk : chunks) {
            try {
                List<Double> vector = embeddingService.generateEmbedding(chunk.getContent());
                String serialized = embeddingService.serializeVector(vector);
                
                ResumeEmbedding embeddingEntity = new ResumeEmbedding(
                    resume, 
                    chunk.getContent(), 
                    chunk.getSectionName(), 
                    serialized
                );
                embeddingsToSave.add(embeddingEntity);
            } catch (Exception e) {
                logger.error("Failed to generate embedding for chunk in resume {}: {}", resume.getId(), e.getMessage());
            }
        }

        if (!embeddingsToSave.isEmpty()) {
            resumeEmbeddingRepository.saveAll(embeddingsToSave);
            logger.info("Successfully indexed {} vector chunks for resume ID: {}", embeddingsToSave.size(), resume.getId());
        }
    }

    /**
     * Finds the overall most semantically similar resume chunks in the database for a search query.
     */
    public List<ResumeEmbedding> searchSimilar(String query, int limit) {
        List<Double> queryVector = embeddingService.generateEmbedding(query);
        List<ResumeEmbedding> allEmbeddings = resumeEmbeddingRepository.findAll();
        
        allEmbeddings.sort((re1, re2) -> {
            double score1 = calculateCosineSimilarity(queryVector, embeddingService.deserializeVector(re1.getEmbeddingString()));
            double score2 = calculateCosineSimilarity(queryVector, embeddingService.deserializeVector(re2.getEmbeddingString()));
            return Double.compare(score2, score1);
        });

        return allEmbeddings.subList(0, Math.min(limit, allEmbeddings.size()));
    }

    /**
     * Scopes similarity search to chunks within a single candidate's resume (highly useful for localized context extraction).
     */
    public List<ResumeEmbedding> searchSimilarForResume(Long resumeId, String query, int limit) {
        List<Double> queryVector = embeddingService.generateEmbedding(query);
        List<ResumeEmbedding> resumeEmbeddings = resumeEmbeddingRepository.findByResumeId(resumeId);

        resumeEmbeddings.sort((re1, re2) -> {
            double score1 = calculateCosineSimilarity(queryVector, embeddingService.deserializeVector(re1.getEmbeddingString()));
            double score2 = calculateCosineSimilarity(queryVector, embeddingService.deserializeVector(re2.getEmbeddingString()));
            return Double.compare(score2, score1);
        });

        return resumeEmbeddings.subList(0, Math.min(limit, resumeEmbeddings.size()));
    }

    /**
     * Computes the Cosine Similarity metric between two high-dimensional vectors.
     */
    public double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            normA += vec1.get(i) * vec1.get(i);
            normB += vec2.get(i) * vec2.get(i);
        }
        return (normA == 0.0 || normB == 0.0) ? 0.0 : (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    /**
     * Smart chunking strategy: splits text by major sections or falls back to logical paragraphs.
     */
    private List<ResumeChunk> chunkResumeText(String text) {
        List<ResumeChunk> chunks = new ArrayList<>();

        // Major section headers pattern
        Pattern sectionPattern = Pattern.compile(
            "(?im)^(education|experience|work|employment|skills|projects|summary|objective|achievements|certifications|languages|interests)\\b"
        );
        Matcher matcher = sectionPattern.matcher(text);

        List<Integer> headerIndices = new ArrayList<>();
        List<String> sectionNames = new ArrayList<>();

        while (matcher.find()) {
            headerIndices.add(matcher.start());
            sectionNames.add(matcher.group().trim());
        }

        // If headers are found, chunk by headers
        if (!headerIndices.isEmpty()) {
            for (int i = 0; i < headerIndices.size(); i++) {
                int start = headerIndices.get(i);
                int end = (i < headerIndices.size() - 1) ? headerIndices.get(i + 1) : text.length();
                
                String sectionContent = text.substring(start, end).trim();
                String sectionName = capitalizeWord(sectionNames.get(i));

                if (sectionContent.length() > 20) { // skip tiny sections
                    // If a section is very large (e.g. experience), sub-chunk it by paragraphs
                    if (sectionContent.length() > 1200) {
                        subChunkLargeSection(sectionContent, sectionName, chunks);
                    } else {
                        chunks.add(new ResumeChunk(sectionContent, sectionName));
                    }
                }
            }
        } else {
            // Fallback: chunk by logical paragraph breaks
            String[] paragraphs = text.split("\\n\\n+");
            StringBuilder currentChunk = new StringBuilder();
            int chunkCount = 1;

            for (String para : paragraphs) {
                para = para.trim();
                if (para.isEmpty()) continue;

                if (currentChunk.length() + para.length() > 800) {
                    if (currentChunk.length() > 0) {
                        chunks.add(new ResumeChunk(currentChunk.toString().trim(), "Bio-Block " + chunkCount++));
                        currentChunk.setLength(0);
                    }
                }
                currentChunk.append(para).append("\n\n");
            }
            if (currentChunk.length() > 0) {
                chunks.add(new ResumeChunk(currentChunk.toString().trim(), "Bio-Block " + chunkCount));
            }
        }

        return chunks;
    }

    private void subChunkLargeSection(String sectionContent, String sectionName, List<ResumeChunk> chunks) {
        String[] blocks = sectionContent.split("\\n{2,}");
        StringBuilder currentSubChunk = new StringBuilder();
        int subIndex = 1;

        for (String block : blocks) {
            block = block.trim();
            if (block.isEmpty()) continue;

            if (currentSubChunk.length() + block.length() > 1000) {
                if (currentSubChunk.length() > 0) {
                    chunks.add(new ResumeChunk(currentSubChunk.toString().trim(), sectionName + " (Part " + subIndex++ + ")"));
                    currentSubChunk.setLength(0);
                }
            }
            currentSubChunk.append(block).append("\n\n");
        }
        if (currentSubChunk.length() > 0) {
            chunks.add(new ResumeChunk(currentSubChunk.toString().trim(), sectionName + " (Part " + subIndex + ")"));
        }
    }

    private String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    /**
     * Local helper class for holding text-segment representations.
     */
    private static class ResumeChunk {
        private final String content;
        private final String sectionName;

        public ResumeChunk(String content, String sectionName) {
            this.content = content;
            this.sectionName = sectionName;
        }

        public String getContent() {
            return content;
        }

        public String getSectionName() {
            return sectionName;
        }
    }
}
