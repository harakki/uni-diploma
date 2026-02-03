package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TagService;
import dev.harakki.comics.catalog.domain.TagType;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import(SecurityConfig.class)
class TagControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    TagService tagService;

    TagCreateRequest tagCreateRequest;

    @BeforeEach
    void setUp() {
        tagCreateRequest = TagCreateRequest.builder()
                .name("Action")
                .type(TagType.GENRE)
                .description("Action genre")
                .build();
    }

    // CREATE TAG TESTS

    @Test
    void createTag_created() throws Exception {
        when(tagService.create(any(TagCreateRequest.class)))
                .thenReturn(new TagResponse(UUID.randomUUID(), "Action", "action", TagType.GENRE, "Action genre"));

        mockMvc.perform(post("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createTag_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTag_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTag_conflict() throws Exception {
        when(tagService.create(any(TagCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Tag exists"));

        mockMvc.perform(post("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isConflict());
    }

    // UPDATE TAG TESTS

    @Test
    void updateTag_ok() throws Exception {
        UUID tagId = UUID.randomUUID();
        var updateRequest = TagUpdateRequest.builder()
                .name("Action Updated")
                .type(TagType.GENRE)
                .build();

        when(tagService.update(eq(tagId), any(TagUpdateRequest.class)))
                .thenReturn(new TagResponse(tagId, "Action Updated", "action", TagType.GENRE, "Action genre"));

        mockMvc.perform(put("/api/v1/tags/{id}", tagId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTag_notFound() throws Exception {
        UUID tagId = UUID.randomUUID();
        var updateRequest = TagUpdateRequest.builder()
                .name("Action Updated")
                .type(TagType.GENRE)
                .build();

        when(tagService.update(eq(tagId), any(TagUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/tags/{id}", tagId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTag_unauthorized() throws Exception {
        UUID tagId = UUID.randomUUID();
        var updateRequest = TagUpdateRequest.builder()
                .name("Action Updated")
                .type(TagType.GENRE)
                .build();

        mockMvc.perform(put("/api/v1/tags/{id}", tagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTag_forbidden() throws Exception {
        UUID tagId = UUID.randomUUID();
        var updateRequest = TagUpdateRequest.builder()
                .name("Action Updated")
                .type(TagType.GENRE)
                .build();

        mockMvc.perform(put("/api/v1/tags/{id}", tagId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // GET TAG BY ID

    @Test
    void getTag_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(tagService.getById(eq(id)))
                .thenReturn(new TagResponse(id, "Action", "action", TagType.GENRE, "Action genre"));

        mockMvc.perform(get("/api/v1/tags/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTag_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(tagService.getById(eq(id)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/tags/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET TAG BY SLUG

    @Test
    void getTagBySlug_ok() throws Exception {
        String slug = "action";
        UUID id = UUID.randomUUID();
        when(tagService.getBySlug(eq(slug)))
                .thenReturn(new TagResponse(id, "Action", slug, TagType.GENRE, "Action genre"));

        mockMvc.perform(get("/api/v1/tags/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTagBySlug_notFound() throws Exception {
        String slug = "unknown";
        when(tagService.getBySlug(eq(slug)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/tags/slug/{slug}", slug)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET ALL TAGS

    @Test
    void getAllTags_ok() throws Exception {
        when(tagService.getAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // DELETE TAG

    @Test
    void deleteTag_noContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tags/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTag_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Not found")).when(tagService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/tags/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTag_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tags/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTag_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tags/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE SLUG

    @Test
    void updateSlug_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(tagService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenReturn(new TagResponse(id, "Action", "new-slug", TagType.GENRE, "Action genre"));

        mockMvc.perform(put("/api/v1/tags/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSlug_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");
        when(tagService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/tags/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSlug_conflict() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("existing-slug");
        when(tagService.updateSlug(eq(id), any(ReplaceSlugRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Slug already exists"));

        mockMvc.perform(put("/api/v1/tags/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSlug_unauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/tags/{id}/slug", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSlug_forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new ReplaceSlugRequest("new-slug");

        mockMvc.perform(put("/api/v1/tags/{id}/slug", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

}
