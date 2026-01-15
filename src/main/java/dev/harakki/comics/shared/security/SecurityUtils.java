package dev.harakki.comics.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Keycloak "sub" claim is the UUID
            return Optional.ofNullable(jwt.getSubject())
                    .map(UUID::fromString);
        }

        return Optional.empty();
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> Objects.equals(a.getAuthority(), AuthoritiesConstants.ADMIN));
    }

    private SecurityUtils() {
        // Prevent instantiation
    }

}
