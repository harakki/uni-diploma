package dev.harakki.comics.collections.web;

import dev.harakki.comics.collections.application.CollectionService;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
import dev.harakki.comics.shared.config.SecurityConfig;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCollectionController.class)
@Import(SecurityConfig.class)
class UserCollectionControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    CollectionService collectionService;

    CollectionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CollectionCreateRequest(
                "My Favorites", "My favorite comics", true, List.of()
        );
    }

    // CREATE COLLECTION TESTS

    @Test
    void createCollection_created() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(collectionService.create(any(CollectionCreateRequest.class)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, null, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/v1/collections")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCollection_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    // GET COLLECTION BY ID

    @Test
    void getById_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(collectionService.getById(eq(collectionId)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, null, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getById_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        when(collectionService.getById(eq(collectionId)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(get("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // SEARCH PUBLIC COLLECTIONS

    @Test
    void search_ok() throws Exception {
        when(collectionService.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/collections")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // GET MY COLLECTIONS

    @Test
    void getMyCollections_ok() throws Exception {
        when(collectionService.getMyCollections(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/collections/my")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMyCollections_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/collections/my"))
                .andExpect(status().isUnauthorized());
    }

    // UPDATE COLLECTION

    @Test
    void updateCollection_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        var updateRequest = new CollectionUpdateRequest("Updated Name", "Updated description", false, List.of());

        when(collectionService.update(eq(collectionId), any(CollectionUpdateRequest.class)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "Updated Name",
                        "Updated description", false, null, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCollection_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        var updateRequest = new CollectionUpdateRequest("Updated Name", "Updated description", false, List.of());

        when(collectionService.update(eq(collectionId), any(CollectionUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCollection_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();
        var updateRequest = new CollectionUpdateRequest("Updated Name", "Updated description", false, List.of());

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCollection_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        var updateRequest = new CollectionUpdateRequest("Updated Name", "Updated description", false, List.of());

        when(collectionService.update(eq(collectionId), any(CollectionUpdateRequest.class)))
                .thenThrow(new AccessDeniedException("You don't have permission to update this collection"));

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // DELETE COLLECTION

    @Test
    void deleteCollection_noContent() throws Exception {
        UUID collectionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCollection_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Collection not found"))
                .when(collectionService).delete(eq(collectionId));

        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCollection_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCollection_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        doThrow(new AccessDeniedException("You don't have permission to delete this collection"))
                .when(collectionService).delete(eq(collectionId));

        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // GENERATE SHARE LINK

    @Test
    void generateShareLink_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(collectionService.generateShareToken(eq(collectionId)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, "share-token-123", List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void generateShareLink_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        when(collectionService.generateShareToken(eq(collectionId)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(post("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateShareLink_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/collections/{id}/share", collectionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generateShareLink_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        when(collectionService.generateShareToken(eq(collectionId)))
                .thenThrow(new AccessDeniedException("You don't have permission to share this collection"));

        mockMvc.perform(post("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // GET COLLECTION BY SHARE TOKEN

    @Test
    void getByShareToken_ok() throws Exception {
        String shareToken = "share-token-123";
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(collectionService.getByShareToken(eq(shareToken)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, shareToken, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/v1/collections/shared/{shareToken}", shareToken)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getByShareToken_notFound() throws Exception {
        String shareToken = "invalid-token";
        when(collectionService.getByShareToken(eq(shareToken)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(get("/api/v1/collections/shared/{shareToken}", shareToken)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // REVOKE SHARE LINK

    @Test
    void revokeShareLink_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(collectionService.revokeShareToken(eq(collectionId)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, null, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(delete("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void revokeShareLink_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        when(collectionService.revokeShareToken(eq(collectionId)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(delete("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void revokeShareLink_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/collections/{id}/share", collectionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeShareLink_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        when(collectionService.revokeShareToken(eq(collectionId)))
                .thenThrow(new AccessDeniedException("You don't have permission to revoke share link"));

        mockMvc.perform(delete("/api/v1/collections/{id}/share", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // ADD TITLES TO COLLECTION

    @Test
    void addTitles_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        List<UUID> titleIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(collectionService.addTitles(eq(collectionId), eq(titleIds)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, null, titleIds, Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/v1/collections/{id}/titles", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleIds)))
                .andExpect(status().isOk());
    }

    @Test
    void addTitles_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        List<UUID> titleIds = List.of(UUID.randomUUID());

        when(collectionService.addTitles(eq(collectionId), eq(titleIds)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(post("/api/v1/collections/{id}/titles", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleIds)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTitles_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();
        List<UUID> titleIds = List.of(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/collections/{id}/titles", collectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleIds)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTitles_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        List<UUID> titleIds = List.of(UUID.randomUUID());

        when(collectionService.addTitles(eq(collectionId), eq(titleIds)))
                .thenThrow(new AccessDeniedException("You don't have permission to update this collection"));

        mockMvc.perform(post("/api/v1/collections/{id}/titles", collectionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleIds)))
                .andExpect(status().isForbidden());
    }

    // REMOVE TITLE FROM COLLECTION

    @Test
    void removeTitle_ok() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        when(collectionService.removeTitle(eq(collectionId), eq(titleId)))
                .thenReturn(new UserCollectionResponse(collectionId, authorId, "My Favorites",
                        "My favorite comics", true, null, List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(delete("/api/v1/collections/{id}/titles/{titleId}", collectionId, titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeTitle_notFound() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();

        when(collectionService.removeTitle(eq(collectionId), eq(titleId)))
                .thenThrow(new ResourceNotFoundException("Collection not found"));

        mockMvc.perform(delete("/api/v1/collections/{id}/titles/{titleId}", collectionId, titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeTitle_unauthorized() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/collections/{id}/titles/{titleId}", collectionId, titleId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeTitle_forbidden() throws Exception {
        UUID collectionId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();

        when(collectionService.removeTitle(eq(collectionId), eq(titleId)))
                .thenThrow(new AccessDeniedException("You don't have permission to update this collection"));

        mockMvc.perform(delete("/api/v1/collections/{id}/titles/{titleId}", collectionId, titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

}
