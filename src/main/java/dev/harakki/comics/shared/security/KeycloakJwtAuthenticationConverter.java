package dev.harakki.comics.shared.security;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractResourceRoles(jwt));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        // Keycloak's JWT roles are stored in the "realm_access" claim as follows:
        // {
        //   ...
        //   "realm_access": {
        //      "roles": ["admin", "user"]
        //   }
        // }
        var realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return List.of();
        }

        // Get the list of roles: ["admin", "user"]
        var roles = (List<String>) realmAccess.get("roles");

        // Convert roles to GrantedAuthority collection
        return roles.stream()
                .map(roleName -> "ROLE_" + roleName.toUpperCase()) // "admin" -> "ROLE_ADMIN"
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

}
