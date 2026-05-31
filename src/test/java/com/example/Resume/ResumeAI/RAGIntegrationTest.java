package com.example.Resume.ResumeAI;

import com.example.Resume.ResumeAI.entity.Resume;
import com.example.Resume.ResumeAI.entity.ResumeEmbedding;
import com.example.Resume.ResumeAI.repository.ResumeEmbeddingRepository;
import com.example.Resume.ResumeAI.repository.ResumeRepository;
import com.example.Resume.ResumeAI.service.AIService;
import com.example.Resume.ResumeAI.service.EmbeddingService;
import com.example.Resume.ResumeAI.service.GeminiAIService;
import com.example.Resume.ResumeAI.service.VectorStoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RAGIntegrationTest {

    private EmbeddingService embeddingService;
    private VectorStoreService vectorStoreService;
    private AIService aiService;

    @Mock
    private ResumeEmbeddingRepository resumeEmbeddingRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private GeminiAIService geminiAIService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        embeddingService = new EmbeddingService(objectMapper);
        vectorStoreService = new VectorStoreService(embeddingService, resumeEmbeddingRepository);
        aiService = new AIService(geminiAIService, vectorStoreService, resumeRepository);
    }

    @Test
    void testEmbeddingFallbackGeneration() {
        List<Double> vector = embeddingService.generateEmbedding("Software Engineering Candidate");
        assertNotNull(vector);
        assertEquals(768, vector.size());

        // L2 Magnitude check (should be normalized close to 1.0)
        double magnitudeSq = 0.0;
        for (double val : vector) {
            magnitudeSq += val * val;
        }
        assertTrue(Math.abs(magnitudeSq - 1.0) < 1e-5, "Fallback vector should be L2 normalized.");
    }

    @Test
    void testVectorSerialization() {
        List<Double> vector = Arrays.asList(0.123, -0.456, 0.789);
        String serialized = embeddingService.serializeVector(vector);
        assertEquals("[0.123,-0.456,0.789]", serialized);
    }

    @Test
    void testResumeSegmentChunkingAndIndexing() {
        Resume resume = new Resume();
        resume.setId(42L);
        resume.setFileName("john_doe_resume.pdf");
        resume.setExtractedText(
            "SUMMARY\n" +
            "Passionate backend developer with 3 years experience.\n\n" +
            "EXPERIENCE\n" +
            "Built smart databases and high throughput APIs.\n\n" +
            "EDUCATION\n" +
            "Bachelor of Science in Computer Science with a focus on Software Engineering."
        );

        // When saving the chunks, capture the entities saved
        ArgumentCaptor<List<ResumeEmbedding>> captor = ArgumentCaptor.forClass(List.class);

        vectorStoreService.indexResume(resume);

        verify(resumeEmbeddingRepository, times(1)).deleteByResumeId(eq(42L));
        verify(resumeEmbeddingRepository, times(1)).saveAll(captor.capture());

        List<ResumeEmbedding> savedEmbeddings = captor.getValue();
        // Should have chunks for Summary, Experience, Education + 1 full text backup = 4 chunks
        assertEquals(4, savedEmbeddings.size());

        // Ensure correct assignment
        ResumeEmbedding summaryChunk = savedEmbeddings.stream()
                .filter(e -> e.getSectionName().equals("Summary"))
                .findFirst()
                .orElse(null);

        assertNotNull(summaryChunk);
        assertTrue(summaryChunk.getContent().contains("Passionate backend developer"));
        assertEquals(42L, summaryChunk.getResume().getId());
    }

    @Test
    void testRAGOrchestratorCoachingResponse() {
        Resume resume = new Resume();
        resume.setId(99L);
        resume.setExtractedText("Full Text Profile");

        // When searching, mock in-memory load
        ResumeEmbedding mockChunk = new ResumeEmbedding(resume, "Expert in Java and Spring Boot APIs.", "Experience", "[0.1,-0.2]");
        when(resumeEmbeddingRepository.findByResumeId(eq(99L)))
                .thenReturn(Arrays.asList(mockChunk));
        when(resumeRepository.findById(99L)).thenReturn(Optional.of(resume));

        when(geminiAIService.generateContentSuggestions(anyString(), anyString()))
                .thenReturn("Your profile highlights strong Spring Boot experience. Highlight that!");

        String advice = aiService.getRAGCoachingResponse(99L, "How do I optimize for Java roles?");

        assertNotNull(advice);
        assertTrue(advice.contains("Spring Boot"));
        
        // Verify RAG prompt gets successfully built with chunk contents injected
        verify(geminiAIService, times(1)).generateContentSuggestions(
            argThat(prompt -> prompt.contains("Expert in Java and Spring Boot APIs.") && prompt.contains("How do I optimize for Java roles?")),
            argThat(persona -> persona.contains("career development coach"))
        );
    }
}
