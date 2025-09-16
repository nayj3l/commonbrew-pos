package com.commonbrew.pos.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // For now, just return a fixed user. Later, integrate with Spring Security.
        return () -> Optional.of("admin"); // or get logged-in user
    }
}

