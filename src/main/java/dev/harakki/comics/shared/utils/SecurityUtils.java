package dev.harakki.comics.shared.utils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public final class SecurityUtils {

    public static Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    public static String getCurrentUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject)
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

    public static Optional<String> getOptionalCurrentUserId() {
        return getCurrentJwt().map(Jwt::getSubject);
    }

    public static String getCurrentUsername() {
        return getCurrentJwt()
                .map(jwt -> jwt.<String>getClaim("preferred_username"))
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

    public static String getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.<String>getClaim("email"))
                .orElseThrow(() -> new AccessDeniedException("No authenticated user found"));
    }

    private SecurityUtils() {
        // Prevent instantiation
    }

}
