package dev.harakki.comics.library.web;

import dev.harakki.comics.library.api.VoteType;
import dev.harakki.comics.library.application.LibraryEntryService;
import dev.harakki.comics.library.domain.ReadingStatus;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEntryController.class)
@Import(SecurityConfig.class)
class LibraryEntryControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    LibraryEntryService libraryEntryService;

    LibraryEntryCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new LibraryEntryCreateRequest(
                UUID.randomUUID(), ReadingStatus.READING, VoteType.LIKE, null
        );
    }

    // ADD TO LIBRARY TESTS

    @Test
    void addToLibrary_created() throws Exception {
        UUID entryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.addToLibrary(any(LibraryEntryCreateRequest.class)))
                .thenReturn(new LibraryEntryResponse(entryId, userId, createRequest.titleId(),
                        ReadingStatus.READING, VoteType.LIKE, null, Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/v1/library/entries")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void addToLibrary_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/library/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addToLibrary_conflict() throws Exception {
        when(libraryEntryService.addToLibrary(any(LibraryEntryCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Title already in library"));

        mockMvc.perform(post("/api/v1/library/entries")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
    }

    // UPDATE LIBRARY ENTRY TESTS

    @Test
    void updateEntry_ok() throws Exception {
        UUID entryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var updateRequest = new LibraryEntryUpdateRequest(ReadingStatus.COMPLETED, VoteType.LIKE, null);

        when(libraryEntryService.update(eq(entryId), any(LibraryEntryUpdateRequest.class)))
                .thenReturn(new LibraryEntryResponse(entryId, userId, UUID.randomUUID(),
                        ReadingStatus.COMPLETED, VoteType.LIKE, null, Instant.now(), Instant.now()));

        mockMvc.perform(put("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateEntry_notFound() throws Exception {
        UUID entryId = UUID.randomUUID();
        var updateRequest = new LibraryEntryUpdateRequest(ReadingStatus.COMPLETED, VoteType.LIKE, null);

        when(libraryEntryService.update(eq(entryId), any(LibraryEntryUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Entry not found"));

        mockMvc.perform(put("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEntry_unauthorized() throws Exception {
        UUID entryId = UUID.randomUUID();
        var updateRequest = new LibraryEntryUpdateRequest(ReadingStatus.COMPLETED, VoteType.LIKE, null);

        mockMvc.perform(put("/api/v1/library/entries/{id}", entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateEntry_forbidden() throws Exception {
        UUID entryId = UUID.randomUUID();
        var updateRequest = new LibraryEntryUpdateRequest(ReadingStatus.COMPLETED, VoteType.LIKE, null);

        when(libraryEntryService.update(eq(entryId), any(LibraryEntryUpdateRequest.class)))
                .thenThrow(new AccessDeniedException("You don't have permission to update this entry"));

        mockMvc.perform(put("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // REMOVE FROM LIBRARY

    @Test
    void removeFromLibrary_noContent() throws Exception {
        UUID entryId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromLibrary_notFound() throws Exception {
        UUID entryId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Entry not found"))
                .when(libraryEntryService).removeFromLibrary(eq(entryId));

        mockMvc.perform(delete("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromLibrary_unauthorized() throws Exception {
        UUID entryId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/library/entries/{id}", entryId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeFromLibrary_forbidden() throws Exception {
        UUID entryId = UUID.randomUUID();
        doThrow(new AccessDeniedException("You don't have permission to delete this entry"))
                .when(libraryEntryService).removeFromLibrary(eq(entryId));

        mockMvc.perform(delete("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // GET LIBRARY ENTRY BY ID

    @Test
    void getEntry_ok() throws Exception {
        UUID entryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.getById(eq(entryId)))
                .thenReturn(new LibraryEntryResponse(entryId, userId, UUID.randomUUID(),
                        ReadingStatus.READING, VoteType.LIKE, null, Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getEntry_notFound() throws Exception {
        UUID entryId = UUID.randomUUID();
        when(libraryEntryService.getById(eq(entryId)))
                .thenThrow(new ResourceNotFoundException("Entry not found"));

        mockMvc.perform(get("/api/v1/library/entries/{id}", entryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEntry_unauthorized() throws Exception {
        UUID entryId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/library/entries/{id}", entryId))
                .andExpect(status().isUnauthorized());
    }

    // GET LIBRARY ENTRY BY TITLE ID

    @Test
    void getByTitleId_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.getByTitleId(eq(titleId)))
                .thenReturn(new LibraryEntryResponse(entryId, userId, titleId,
                        ReadingStatus.READING, VoteType.LIKE, null, Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/v1/library/entries/by-title/{titleId}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getByTitleId_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(libraryEntryService.getByTitleId(eq(titleId)))
                .thenThrow(new ResourceNotFoundException("Entry not found"));

        mockMvc.perform(get("/api/v1/library/entries/by-title/{titleId}", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByTitleId_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/library/entries/by-title/{titleId}", titleId))
                .andExpect(status().isUnauthorized());
    }

    // GET MY LIBRARY

    @Test
    void getMyLibrary_ok() throws Exception {
        when(libraryEntryService.searchLibrary(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/library/entries")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMyLibrary_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/library/entries"))
                .andExpect(status().isUnauthorized());
    }

    // GET LIBRARY BY STATUS

    @Test
    void getByStatus_ok() throws Exception {
        when(libraryEntryService.getMyLibraryByStatus(eq(ReadingStatus.READING), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/library/entries/status/{status}", ReadingStatus.READING)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getByStatus_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/library/entries/status/{status}", ReadingStatus.READING))
                .andExpect(status().isUnauthorized());
    }

    // GET USER LIBRARY

    @Test
    void getUserLibrary_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.getUserLibrary(eq(userId), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/library/users/{userId}/entries", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getUserLibrary_notFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.getUserLibrary(eq(userId), any()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/library/users/{userId}/entries", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserLibrary_forbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        when(libraryEntryService.getUserLibrary(eq(userId), any()))
                .thenThrow(new AccessDeniedException("You don't have permission to view this user's library"));

        mockMvc.perform(get("/api/v1/library/users/{userId}/entries", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserLibrary_unauthorized() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/library/users/{userId}/entries", userId))
                .andExpect(status().isUnauthorized());
    }

}
