package com.example.Resume.ResumeAI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ Jackson configuration for Java 8 Date/Time (LocalDateTime, etc.)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // ✅ CORS configuration for React frontend
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",  // Create React App
                    "http://localhost:5173"   // ← ADD THIS - Vite default port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // ← Add OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}