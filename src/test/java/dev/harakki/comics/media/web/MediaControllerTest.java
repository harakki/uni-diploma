package dev.harakki.comics.media.web;

import dev.harakki.comics.media.application.MediaService;
import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediaController.class)
@Import(SecurityConfig.class)
class MediaControllerTest {

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @MockitoBean
    MediaService mediaService;

    MediaUploadUrlRequest uploadRequest;

    @BeforeEach
    void setUp() {
        uploadRequest = new MediaUploadUrlRequest("cover.jpg", "image/jpeg", 800, 1200);
    }

    // GENERATE UPLOAD URL TESTS

    @Test
    void createMedia_ok() throws Exception {
        UUID mediaId = UUID.randomUUID();
        when(mediaService.getUploadUrl(any(), any(), any(), any()))
                .thenReturn(new MediaUploadUrlResponse(mediaId,
                        "https://s3.example.com/upload?signature=abc", "uploads/" + mediaId + "/cover.jpg"));

        mockMvc.perform(post("/api/v1/media/upload-url")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void createMedia_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMedia_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/media/upload-url")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isForbidden());
    }

    // GET MEDIA URL TESTS

    @Test
    void getMediaUrl_ok() throws Exception {
        UUID mediaId = UUID.randomUUID();
        when(mediaService.getPublicUrl(eq(mediaId)))
                .thenReturn("https://s3.example.com/uploads/" + mediaId + "/cover.jpg?signature=xyz");

        mockMvc.perform(get("/api/v1/media/{id}/url", mediaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMediaUrl_notFound() throws Exception {
        UUID mediaId = UUID.randomUUID();
        when(mediaService.getPublicUrl(eq(mediaId)))
                .thenThrow(new ResourceNotFoundException("Media not found"));

        mockMvc.perform(get("/api/v1/media/{id}/url", mediaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMediaUrl_unauthorized() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/media/{id}/url", mediaId))
                .andExpect(status().isUnauthorized());
    }

    // DELETE MEDIA TESTS

    @Test
    void deleteMedia_noContent() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/media/{id}", mediaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMedia_notFound() throws Exception {
        UUID mediaId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Media not found"))
                .when(mediaService).deleteMediaById(eq(mediaId));

        mockMvc.perform(delete("/api/v1/media/{id}", mediaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMedia_unauthorized() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/media/{id}", mediaId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMedia_forbidden() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/media/{id}", mediaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

}
