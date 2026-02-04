package dev.harakki.comics.shared.config;

import dev.harakki.comics.shared.utils.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

@Configuration
@EnableJpaAuditing // Enable JPA Auditing for @CreatedBy, @LastModifiedBy, etc.
class AuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return SecurityUtils::getOptionalCurrentUserId;
    }

}
