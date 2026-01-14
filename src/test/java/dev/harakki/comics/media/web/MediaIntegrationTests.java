package dev.harakki.comics.media.web;

import dev.harakki.comics.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import dev.harakki.comics.BaseIntegrationTest;
import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MediaIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldGeneratePresignedUploadUrl() throws Exception {
        var request = new MediaUploadUrlRequest("cover.jpg", "image/jpeg", 1000, 1500);

        var result = mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.url", containsString("http://")))
                .andExpect(jsonPath("$.url", containsString("uploads/")))
                .andReturn();

        var response = jsonMapper.readValue(result.getResponse().getContentAsString(), MediaUploadUrlResponse.class);

        // Clean up
        mockMvc.perform(delete("/api/v1/media/" + response.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldValidateInvalidInput() throws Exception {
        // Invalid data
        var request = new MediaUploadUrlRequest("", "image/jpeg", -10, 0);

        mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", containsString("Validation Failed")));
    }

    @Test
    void shouldGetMediaDownloadUrl() throws Exception {
        // Create
        var req = new MediaUploadUrlRequest("test.png", "image/png", 100, 100);
        var resStr = mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        var id = jsonMapper.readValue(resStr, MediaUploadUrlResponse.class).id();

        // Get URL
        mockMvc.perform(get("/api/v1/media/" + id + "/url"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http://")));
    }

    @Test
    void shouldDeleteMedia() throws Exception {
        // Create
        var req = new MediaUploadUrlRequest("del.png", "image/png", 100, 100);
        var resStr = mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        var id = jsonMapper.readValue(resStr, MediaUploadUrlResponse.class).id();

        // Delete
        mockMvc.perform(delete("/api/v1/media/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/media/" + id + "/url"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

}
