package dev.harakki.comics.content.web;

import dev.harakki.comics.content.application.ChapterService;
import dev.harakki.comics.content.dto.*;
import dev.harakki.comics.shared.config.SecurityConfig;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(ChapterController.class)
@Import(SecurityConfig.class)
class ChapterControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    ChapterService chapterService;

    ChapterCreateRequest chapterCreateRequest;

    @BeforeEach
    void setUp() {
        chapterCreateRequest = new ChapterCreateRequest(
                1, 0, "Chapter 1", 1, List.of(UUID.randomUUID())
        );
    }

    // CREATE CHAPTER TESTS

    @Test
    void createChapter_created() throws Exception {
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapterCreateRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createChapter_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters", titleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapterCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createChapter_forbidden() throws Exception {
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapterCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createChapter_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Title not found"))
                .when(chapterService).create(eq(titleId), any(ChapterCreateRequest.class));

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapterCreateRequest)))
                .andExpect(status().isNotFound());
    }

    // GET TITLE CHAPTERS

    @Test
    void getTitleChapters_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(chapterService.getChaptersByTitle(eq(titleId)))
                .thenReturn(List.of(new ChapterSummaryResponse(UUID.randomUUID(), "1", "Chapter 1", 1)));

        mockMvc.perform(get("/api/v1/titles/{titleId}/chapters", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTitleChapters_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        when(chapterService.getChaptersByTitle(eq(titleId)))
                .thenThrow(new ResourceNotFoundException("Title not found"));

        mockMvc.perform(get("/api/v1/titles/{titleId}/chapters", titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET CHAPTER DETAILS

    @Test
    void getChapterDetails_ok() throws Exception {
        UUID chapterId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();
        when(chapterService.getChapterDetails(eq(chapterId)))
                .thenReturn(new ChapterDetailsResponse(chapterId, titleId, "1", "Chapter 1", List.of()));

        mockMvc.perform(get("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getChapterDetails_notFound() throws Exception {
        UUID chapterId = UUID.randomUUID();
        when(chapterService.getChapterDetails(eq(chapterId)))
                .thenThrow(new ResourceNotFoundException("Chapter not found"));

        mockMvc.perform(get("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // UPDATE CHAPTER

    @Test
    void updateChapter_ok() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var updateRequest = new ChapterUpdateRequest(2, 0, "Chapter 2 Updated", 1);

        mockMvc.perform(put("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateChapter_notFound() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var updateRequest = new ChapterUpdateRequest(2, 0, "Chapter 2 Updated", 1);
        doThrow(new ResourceNotFoundException("Chapter not found"))
                .when(chapterService).updateMetadata(eq(chapterId), any(ChapterUpdateRequest.class));

        mockMvc.perform(put("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChapter_unauthorized() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var updateRequest = new ChapterUpdateRequest(2, 0, "Chapter 2 Updated", 1);

        mockMvc.perform(put("/api/v1/chapters/{chapterId}", chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateChapter_forbidden() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var updateRequest = new ChapterUpdateRequest(2, 0, "Chapter 2 Updated", 1);

        mockMvc.perform(put("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // DELETE CHAPTER

    @Test
    void deleteChapter_noContent() throws Exception {
        UUID chapterId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteChapter_notFound() throws Exception {
        UUID chapterId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Chapter not found"))
                .when(chapterService).delete(eq(chapterId));

        mockMvc.perform(delete("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteChapter_unauthorized() throws Exception {
        UUID chapterId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/chapters/{chapterId}", chapterId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteChapter_forbidden() throws Exception {
        UUID chapterId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/chapters/{chapterId}", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // UPDATE CHAPTER PAGES

    @Test
    void updatePages_noContent() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterPagesUpdateRequest(List.of(UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(put("/api/v1/chapters/{chapterId}/pages", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePages_notFound() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterPagesUpdateRequest(List.of(UUID.randomUUID()));
        doThrow(new ResourceNotFoundException("Chapter not found"))
                .when(chapterService).updatePages(eq(chapterId), any());

        mockMvc.perform(put("/api/v1/chapters/{chapterId}/pages", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePages_unauthorized() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterPagesUpdateRequest(List.of(UUID.randomUUID()));

        mockMvc.perform(put("/api/v1/chapters/{chapterId}/pages", chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePages_forbidden() throws Exception {
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterPagesUpdateRequest(List.of(UUID.randomUUID()));

        mockMvc.perform(put("/api/v1/chapters/{chapterId}/pages", chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // RECORD CHAPTER READ

    @Test
    void recordChapterRead_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterReadRequest(UUID.randomUUID(), 5000L);

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void recordChapterRead_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterReadRequest(UUID.randomUUID(), 5000L);

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recordChapterRead_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        var request = new ChapterReadRequest(UUID.randomUUID(), 5000L);
        doThrow(new ResourceNotFoundException("Chapter not found"))
                .when(chapterService).recordChapterRead(eq(chapterId), eq(titleId), any());

        mockMvc.perform(post("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // IS CHAPTER READ

    @Test
    void isChapterRead_ok() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(chapterService.isChapterRead(eq(chapterId), eq(titleId), eq(userId)))
                .thenReturn(new ChapterReadStatusResponse(chapterId, true));

        mockMvc.perform(get("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .param("userId", userId.toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void isChapterRead_unauthorized() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .param("userId", userId.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void isChapterRead_notFound() throws Exception {
        UUID titleId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(chapterService.isChapterRead(eq(chapterId), eq(titleId), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Chapter not found"));

        mockMvc.perform(get("/api/v1/titles/{titleId}/chapters/{chapterId}/read", titleId, chapterId)
                        .param("userId", userId.toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    // GET NEXT UNREAD CHAPTER

    @Test
    void getNextUnreadChapter_ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();
        UUID nextChapterId = UUID.randomUUID();
        when(chapterService.getNextUnreadChapter(eq(userId), eq(titleId)))
                .thenReturn(new NextChapterResponse(nextChapterId, "2", "Chapter 2", true));

        mockMvc.perform(get("/api/v1/users/{userId}/titles/{titleId}/next-chapter", userId, titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getNextUnreadChapter_notFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();
        when(chapterService.getNextUnreadChapter(eq(userId), eq(titleId)))
                .thenThrow(new ResourceNotFoundException("Title not found"));

        mockMvc.perform(get("/api/v1/users/{userId}/titles/{titleId}/next-chapter", userId, titleId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNextUnreadChapter_unauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID titleId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/{userId}/titles/{titleId}/next-chapter", userId, titleId))
                .andExpect(status().isUnauthorized());
    }

}
