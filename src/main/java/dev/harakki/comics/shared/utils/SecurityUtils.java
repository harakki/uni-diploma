package dev.harakki.comics.shared.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class SecurityUtils {

    public Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    public UUID getCurrentUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject)
                .map(UUID::fromString)
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

    public Optional<UUID> getOptionalCurrentUserId() {
        return getCurrentJwt().map(Jwt::getSubject).map(UUID::fromString);
    }

    public String getCurrentUserUsername() {
        return getCurrentJwt()
                .map(jwt -> jwt.<String>getClaim("preferred_username"))
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

    public String getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.<String>getClaim("email"))
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

}
