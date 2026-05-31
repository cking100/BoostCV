package com.example.Resume.ResumeAI.config;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsRaw;

    // ✅ Jackson configuration for Java 8 Date/Time (LocalDateTime, etc.)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    /**
     * MVC-level CORS fallback — kept in sync with SecurityConfig.corsConfigurationSource().
     * NOTE: For secured endpoints, Spring Security's CorsConfigurationSource bean in
     * SecurityConfig.java is the authoritative filter and takes precedence over this.
     * This mapping applies to any non-security-filtered routes (e.g. health endpoint).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] defaultOrigins = {
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        };

        String[] extraOrigins = (allowedOriginsRaw != null && !allowedOriginsRaw.isBlank())
            ? Arrays.stream(allowedOriginsRaw.split(",")).map(String::trim).toArray(String[]::new)
            : new String[]{};

        String[] allOrigins = Stream.concat(Arrays.stream(defaultOrigins), Arrays.stream(extraOrigins))
            .distinct().toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(allOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}