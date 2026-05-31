package com.example.Resume.ResumeAI.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Registers a CorsFilter at the HIGHEST_PRECEDENCE so CORS headers are always
 * set before Spring Security or any other filter can reject the request.
 *
 * This is the "nuclear option" guarantee: even if the security filter chain
 * throws or short-circuits, the preflight response will still carry the
 * correct Access-Control-Allow-Origin header.
 *
 * The actual CORS configuration is defined once in
 * {@link SecurityConfig#corsConfigurationSource()} and reused here.
 */
@Configuration
public class CorsFilterConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public CorsFilterConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
        // Run before everything else
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
