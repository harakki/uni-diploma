package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.AuthorService;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
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

@WebMvcTest(AuthorController.class)
@Import(SecurityConfig.class)
class AuthorControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    AuthorService authorService;

    AuthorCreateRequest authorCreateRequest;

    @BeforeEach
    void setUp() {
        authorCreateRequest = AuthorCreateRequest.builder()
                .name("Tatsuki Fujimoto")
                .description("Japanese manga artist")
                .countryIsoCode("JP")
                .websiteUrls(List.of("https://twitter.com/tatsuki_fujimoto"))
                .build();
    }

    // CREATE AUTHOR TESTS

    @Test
    void createAuthor_created() throws Exception {
        when(authorService.create(any(AuthorCreateRequest.class)))
                .thenReturn(new AuthorResponse(UUID.randomUUID(), "Tatsuki Fujimoto", "tatsuki-fujimoto",
                        "Japanese manga artist", List.of("https://twitter.com/tatsuki_fujimoto"), "JP", null));

        mockMvc.perform(post("/api/v1/authors")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorCreateRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createAuthor_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAuthor_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAuthor_conflict() throws Exception {
        when(authorService.create(any(AuthorCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Author exists"));

        mockMvc.perform(post("/api/v1/authors")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorCreateRequest)))
                .andExpect(status().isConflict());
    }

    // UPDATE AUTHOR TESTS

    @Test
    void updateAuthor_ok() throws Exception {
        UUID authorId = UUID.randomUUID();
        var updateRequest = new AuthorUpdateRequest("Tatsuki Fujimoto Updated", null, null, null, null);

        when(authorService.update(eq(authorId), any(AuthorUpdateRequest.class)))
                .thenReturn(new AuthorResponse(authorId, "Tatsuki Fujimoto Updated", "tatsuki-fujimoto",
                        "Japanese manga artist", List.of(), "JP", null));

        mockMvc.perform(put("/api/v1/authors/{id}", authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAuthor_notFound() throws Exception {
        UUID authorId = UUID.randomUUID();
        var updateRequest = new AuthorUpdateRequest("Tatsuki Fujimoto Updated", null, null, null, null);

        when(authorService.update(eq(authorId), any(AuthorUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/authors/{id}", authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAuthor_unauthorized() throws Exception {
        UUID authorId = UUID.randomUUID();
        var updateRequest = new AuthorUpdateRequest("Tatsuki Fujimoto Updated", null, null, null, null);

        mockMvc.perform(put("/api/v1/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAuthor_forbidden() throws Exception {
        UUID authorId = UUID.randomUUID();
        var updateRequest = new AuthorUpdateRequest("Tatsuki Fujimoto Updated", null, null, null, null);

        mockMvc.perform(put("/api/v1/authors/{id}", authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // GET AUTHOR BY ID

    @Test
    void getAuthor_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(authorService.getById(eq(id)))
                .thenReturn(new AuthorResponse(id, "Tatsuki Fujimoto", "tatsuki-fujimoto",
                        "Japanese manga artist", List.of(), "JP", null));

        mockMvc.perform(get("/api/v1/authors/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthor_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(authorService.getById(eq(id)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/authors/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET AUTHOR BY SLUG

    @Test
    void getAuthorBySlug_ok() throws Exception {
        String slug = "tatsuki-fujimoto";
        UUID id = UUID.randomUUID();
        when(authorService.getBySlug(eq(slug)))
                .thenReturn(new AuthorResponse(id, "Tatsuki Fujimoto", slug,
                        "Japanese manga artist", List.of(), "JP", null));

        mockMvc.perform(get("/api/v1/authors/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthorBySlug_notFound() throws Exception {
        String slug = "unknown";
        when(authorService.getBySlug(eq(slug)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/authors/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // SEARCH AUTHORS

    @Test
    void searchAuthors_ok() throws Exception {
        when(authorService.getAll(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/authors")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // DELETE AUTHOR

    @Test
    void deleteAuthor_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/authors/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuthor_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Not found")).when(authorService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/authors/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/authors/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAuthor_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/authors/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE SLUG

    @Test
    void updateSlug_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(authorService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenReturn(new AuthorResponse(id, "Tatsuki Fujimoto", "new-slug",
                        "Japanese manga artist", List.of(), "JP", null));

        mockMvc.perform(put("/api/v1/authors/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSlug_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(authorService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/authors/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSlug_conflict() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("existing-slug");
        when(authorService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Slug already exists"));

        mockMvc.perform(put("/api/v1/authors/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSlug_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/authors/{id}/slug", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSlug_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/authors/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

}
