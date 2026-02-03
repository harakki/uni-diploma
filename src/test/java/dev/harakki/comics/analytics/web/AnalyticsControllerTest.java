package dev.harakki.comics.analytics.web;

import dev.harakki.comics.analytics.application.AnalyticsService;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import dev.harakki.comics.shared.config.SecurityConfig;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AnalyticsService analyticsService;

    // GET TITLE ANALYTICS

    @Test
    void getTitleAnalytics_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(analyticsService.getTitleAnalytics(eq(titleId)))
                .thenReturn(new TitleAnalyticsResponse(titleId, 4.5, 1000L, Instant.now()));

        mockMvc.perform(get("/api/v1/analytics/titles/{titleId}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTitleAnalytics_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(analyticsService.getTitleAnalytics(eq(titleId)))
                .thenThrow(new ResourceNotFoundException("Title not found"));

        mockMvc.perform(get("/api/v1/analytics/titles/{titleId}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTitleAnalytics_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/analytics/titles/{titleId}", titleId))
                .andExpect(status().isUnauthorized());
    }

}
