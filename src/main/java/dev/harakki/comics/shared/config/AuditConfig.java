package dev.harakki.comics.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // TODO: Setup SecurityContextHolder when auth will be ready
        return Optional::empty;
    }

}
