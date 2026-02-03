package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TitleService;
import dev.harakki.comics.catalog.domain.AuthorRole;
import dev.harakki.comics.catalog.domain.ContentRating;
import dev.harakki.comics.catalog.domain.TitleStatus;
import dev.harakki.comics.catalog.domain.TitleType;
import dev.harakki.comics.catalog.dto.*;
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

import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TitleController.class)
@Import(SecurityConfig.class)
class TitleControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    TitleService titleService;

    TitleCreateRequest title;

    @BeforeEach
    void setUp() {
        title = TitleCreateRequest.builder()
                .name("Chainsaw Man")
                .type(TitleType.COMIC)
                .titleStatus(TitleStatus.COMPLETED)
                .releaseYear(Year.of(2018))
                .contentRating(ContentRating.SIXTEEN_PLUS)
                .countryIsoCode("JP")
                .build();
    }

    // CREATE TITLE TESTS

    @Test
    void createTitle_created() throws Exception {
        when(titleService.create(any(TitleCreateRequest.class)))
                .thenReturn(new TitleResponse(UUID.randomUUID(), "Chainsaw Man", "chainsaw-man", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(post("/api/v1/titles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isCreated());
    }

    @Test
    void createTitle_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTitle_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/titles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTitle_conflict() throws Exception {
        when(titleService.create(any(TitleCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Title exists"));

        mockMvc.perform(post("/api/v1/titles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isConflict());
    }

    // UPDATE TITLE TESTS

    @Test
    void updateTitle_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(titleService.update(eq(titleId), any(TitleUpdateRequest.class)))
                .thenReturn(new TitleResponse(titleId, "Chainsaw Man", "chainsaw-man", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(put("/api/v1/titles" + "/{id}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTitle_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(titleService.update(eq(titleId), any(TitleUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/titles" + "/{id}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTitle_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/titles" + "/{id}", titleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTitle_forbidden() throws Exception {
        UUID titleId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/titles" + "/{id}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(title)))
                .andExpect(status().isForbidden());
    }

    // GET TITLE BY ID

    @Test
    void getTitle_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(titleService.getById(eq(id))).thenReturn(new TitleResponse(id, "Chainsaw Man", "chainsaw-man", null,
                TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(get("/api/v1/titles/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTitle_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(titleService.getById(eq(id))).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/titles/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // GET TITLE BY SLUG

    @Test
    void getTitleBySlug_ok() throws Exception {
        String slug = "chainsaw-man";
        UUID id = UUID.randomUUID();
        when(titleService.getBySlug(eq(slug))).thenReturn(new TitleResponse(id, "Chainsaw Man", slug, null,
                TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(get("/api/v1/titles/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTitleBySlug_notFound() throws Exception {
        String slug = "unknown";
        when(titleService.getBySlug(eq(slug))).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/titles/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // SEARCH TITLES

    @Test
    void searchTitles_ok() throws Exception {
        when(titleService.getAll(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/titles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    // DELETE TITLE

    @Test
    void deleteTitle_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/titles/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTitle_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Not found")).when(titleService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/titles/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTitle_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/titles/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTitle_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/titles/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE SLUG

    @Test
    void updateSlug_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(titleService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenReturn(new TitleResponse(id, "Chainsaw Man", "new-slug", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(put("/api/v1/titles/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSlug_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(titleService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/titles/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSlug_conflict() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(titleService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Conflict"));

        mockMvc.perform(put("/api/v1/titles/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSlug_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/titles/{id}/slug", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSlug_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/titles/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ADD AUTHOR

    @Test
    void addAuthor_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new TitleAddAuthorRequest(UUID.randomUUID(), AuthorRole.STORY);
        when(titleService.addAuthor(eq(id), eq(req.authorId()), eq(req.role())))
                .thenReturn(new TitleResponse(id, "Chainsaw Man", "slug", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(post("/api/v1/titles/{id}/authors", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void addAuthor_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new TitleAddAuthorRequest(UUID.randomUUID(), dev.harakki.comics.catalog.domain.AuthorRole.STORY);
        when(titleService.addAuthor(eq(id), eq(req.authorId()), eq(req.role())))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/titles/{id}/authors", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAuthor_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new TitleAddAuthorRequest(UUID.randomUUID(), AuthorRole.STORY);

        mockMvc.perform(post("/api/v1/titles/{id}/authors", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addAuthor_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new TitleAddAuthorRequest(UUID.randomUUID(), AuthorRole.STORY);

        mockMvc.perform(post("/api/v1/titles/{id}/authors", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // REMOVE AUTHOR

    @Test
    void removeAuthor_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(titleService.removeAuthor(eq(id), eq(authorId)))
                .thenReturn(new TitleResponse(id, "Chainsaw Man", "slug", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(delete("/api/v1/titles/{id}/authors/{authorId}", id, authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeAuthor_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(titleService.removeAuthor(eq(id), eq(authorId))).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(delete("/api/v1/titles/{id}/authors/{authorId}", id, authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeAuthor_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/titles/{id}/authors/{authorId}", id, authorId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeAuthor_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/titles/{id}/authors/{authorId}", id, authorId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // REMOVE PUBLISHER

    @Test
    void removePublisher_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(titleService.removePublisher(eq(id))).thenReturn(new TitleResponse(id, "Chainsaw Man", "slug", null,
                TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(delete("/api/v1/titles/{id}/publisher", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removePublisher_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(titleService.removePublisher(eq(id))).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(delete("/api/v1/titles/{id}/publisher", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removePublisher_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/titles/{id}/publisher", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removePublisher_forbidden() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/titles/{id}/publisher", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE TAGS

    @Test
    void updateTags_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceTagsRequest(Set.of(UUID.randomUUID()));
        when(titleService.updateTags(eq(id), any(ReplaceTagsRequest.class)))
                .thenReturn(new TitleResponse(id, "Chainsaw Man", "slug", null,
                        TitleType.COMIC, TitleStatus.COMPLETED, Year.of(2018), ContentRating.SIXTEEN_PLUS,
                        false, "JP", null, List.of(), null, Set.of()));

        mockMvc.perform(post("/api/v1/titles/{id}/tags", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTags_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceTagsRequest(Set.of(UUID.randomUUID()));
        when(titleService.updateTags(eq(id), any(ReplaceTagsRequest.class))).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/titles/{id}/tags", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTags_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceTagsRequest(Set.of(UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/titles/{id}/tags", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTags_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceTagsRequest(Set.of(UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/titles/{id}/tags", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

}
