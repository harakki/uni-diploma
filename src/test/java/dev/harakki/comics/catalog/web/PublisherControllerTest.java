package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.shared.config.SecurityConfig;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublisherController.class)
@Import(SecurityConfig.class)
class PublisherControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    PublisherService publisherService;

    PublisherCreateRequest publisherCreateRequest;

    @BeforeEach
    void setUp() {
        publisherCreateRequest = PublisherCreateRequest.builder()
                .name("Shueisha")
                .description("Japanese publisher")
                .countryIsoCode("JP")
                .websiteUrls(List.of("https://www.shueisha.co.jp"))
                .build();
    }

    // CREATE PUBLISHER TESTS

    @Test
    void createPublisher_created() throws Exception {
        when(publisherService.create(any(PublisherCreateRequest.class)))
                .thenReturn(new PublisherResponse(UUID.randomUUID(), "Shueisha", "shueisha",
                        "Japanese publisher", List.of("https://www.shueisha.co.jp"), "JP", null));

        mockMvc.perform(post("/api/v1/publishers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(publisherCreateRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPublisher_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(publisherCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPublisher_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/publishers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(publisherCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPublisher_conflict() throws Exception {
        when(publisherService.create(any(PublisherCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Publisher exists"));

        mockMvc.perform(post("/api/v1/publishers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(publisherCreateRequest)))
                .andExpect(status().isConflict());
    }

    // UPDATE PUBLISHER TESTS

    @Test
    void updatePublisher_ok() throws Exception {
        UUID publisherId = UUID.randomUUID();
        var updateRequest = new PublisherUpdateRequest("Shueisha Updated", null, null, null, null);

        when(publisherService.update(eq(publisherId), any(PublisherUpdateRequest.class)))
                .thenReturn(new PublisherResponse(publisherId, "Shueisha Updated", "shueisha",
                        "Japanese publisher", List.of(), "JP", null));

        mockMvc.perform(put("/api/v1/publishers/{id}", publisherId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePublisher_notFound() throws Exception {
        UUID publisherId = UUID.randomUUID();
        var updateRequest = new PublisherUpdateRequest("Shueisha Updated", null, null, null, null);

        when(publisherService.update(eq(publisherId), any(PublisherUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/publishers/{id}", publisherId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePublisher_unauthorized() throws Exception {
        UUID publisherId = UUID.randomUUID();
        var updateRequest = new PublisherUpdateRequest("Shueisha Updated", null, null, null, null);

        mockMvc.perform(put("/api/v1/publishers/{id}", publisherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePublisher_forbidden() throws Exception {
        UUID publisherId = UUID.randomUUID();
        var updateRequest = new PublisherUpdateRequest("Shueisha Updated", null, null, null, null);

        mockMvc.perform(put("/api/v1/publishers/{id}", publisherId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // GET PUBLISHER BY ID

    @Test
    void getPublisher_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(publisherService.getById(eq(id)))
                .thenReturn(new PublisherResponse(id, "Shueisha", "shueisha",
                        "Japanese publisher", List.of(), "JP", null));

        mockMvc.perform(get("/api/v1/publishers/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getPublisher_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(publisherService.getById(eq(id)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/publishers/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET PUBLISHER BY SLUG

    @Test
    void getPublisherBySlug_ok() throws Exception {
        String slug = "shueisha";
        UUID id = UUID.randomUUID();
        when(publisherService.getBySlug(eq(slug)))
                .thenReturn(new PublisherResponse(id, "Shueisha", slug,
                        "Japanese publisher", List.of(), "JP", null));

        mockMvc.perform(get("/api/v1/publishers/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getPublisherBySlug_notFound() throws Exception {
        String slug = "unknown";
        when(publisherService.getBySlug(eq(slug)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/publishers/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // SEARCH PUBLISHERS

    @Test
    void searchPublishers_ok() throws Exception {
        when(publisherService.getAll(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/publishers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // DELETE PUBLISHER

    @Test
    void deletePublisher_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/publishers/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePublisher_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Not found")).when(publisherService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/publishers/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePublisher_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/publishers/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePublisher_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/publishers/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE SLUG

    @Test
    void updateSlug_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(publisherService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenReturn(new PublisherResponse(id, "Shueisha", "new-slug",
                        "Japanese publisher", List.of(), "JP", null));

        mockMvc.perform(put("/api/v1/publishers/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSlug_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(publisherService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/publishers/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSlug_conflict() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("existing-slug");
        when(publisherService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Slug already exists"));

        mockMvc.perform(put("/api/v1/publishers/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSlug_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/publishers/{id}/slug", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSlug_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/publishers/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

}
