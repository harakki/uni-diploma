package dev.harakki.comics.content.web;

import dev.harakki.comics.BaseIntegrationTest;
import dev.harakki.comics.catalog.domain.ContentRating;
import dev.harakki.comics.catalog.domain.TitleStatus;
import dev.harakki.comics.catalog.domain.TitleType;
import dev.harakki.comics.catalog.dto.TitleCreateRequest;
import dev.harakki.comics.catalog.dto.TitleResponse;
import dev.harakki.comics.content.dto.ChapterCreateRequest;
import dev.harakki.comics.content.dto.ChapterPagesUpdateRequest;
import dev.harakki.comics.content.dto.ChapterUpdateRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

class ChapterIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    private UUID titleId;
    private UUID mediaId1;
    private UUID mediaId2;

    @BeforeEach
    void setupData() throws Exception {
        // Create Title
        var titleReq = new TitleCreateRequest("Naruto", "Ninjas", TitleType.MANGA, TitleStatus.COMPLETED, Year.of(1999), ContentRating.SAFE, "JP", null, null, null, null);
        var titleResStr = mockMvc.perform(post("/api/v1/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        this.titleId = jsonMapper.readValue(titleResStr, TitleResponse.class).id();

        // Create fake media entries (upload urls) to link as pages
        this.mediaId1 = createMedia("page1.jpg");
        this.mediaId2 = createMedia("page2.jpg");
    }

    @Test
    void shouldCreateAndRetrieveChapter() throws Exception {
        // Create Chapter
        var chapterCreateRequest = new ChapterCreateRequest(1, 0, "Enter Naruto", 1, List.of(mediaId1, mediaId2));

        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapterCreateRequest)))
                .andExpect(status().isCreated());

        // List Chapters for Title
        var listResult = mockMvc.perform(get("/api/v1/titles/" + titleId + "/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Enter Naruto")))
                .andReturn();

        // Extract Chapter ID from summary list
        String chapterIdStr = jsonMapper.readTree(listResult.getResponse().getContentAsString()).get(0).get("id").asString();

        // 3. Get Chapter Details (with pages)
        mockMvc.perform(get("/api/v1/chapters/" + chapterIdStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pages", hasSize(2)))
                .andExpect(jsonPath("$.pages[0].mediaId", is(mediaId1.toString())))
                .andExpect(jsonPath("$.pages[1].mediaId", is(mediaId2.toString())));
    }

    @Test
    void shouldPreventDuplicateChapterNumbers() throws Exception {
        // Create Chapter 1
        var chapter1 = new ChapterCreateRequest(1, 0, "Ch 1", null, List.of(mediaId1));
        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapter1)))
                .andExpect(status().isCreated());

        // Attempt to create another Chapter 1
        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(chapter1)))
                .andExpect(status().isInternalServerError()); // TODO isConflict(), if add validation in ControllerAdvice
    }

    @Test
    void shouldUpdatePagesList() throws Exception {
        // Create Chapter with mediaId1
        var createReq = new ChapterCreateRequest(2, 0, "Update Me", null, List.of(mediaId1));
        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // Get Chapter ID
        var chapters = mockMvc.perform(get("/api/v1/titles/" + titleId + "/chapters"))
                .andReturn().getResponse().getContentAsString();
        var chapterId = jsonMapper.readTree(chapters).get(0).get("id").asString();

        // Update Chapter to have only mediaId2
        var updateReq = new ChapterPagesUpdateRequest(List.of(mediaId2));

        mockMvc.perform(put("/api/v1/chapters/" + chapterId + "/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNoContent());

        // Verify update
        mockMvc.perform(get("/api/v1/chapters/" + chapterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pages", hasSize(1)))
                .andExpect(jsonPath("$.pages[0].mediaId", is(mediaId2.toString())));
    }

    @Test
    void shouldUpdateChapterMetadata() throws Exception {
        // Create
        var createReq = new ChapterCreateRequest(10, 0, "Old Name", null, List.of(mediaId1));
        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        var chapterId = getFirstChapterId();

        // Update
        var updateReq = new ChapterUpdateRequest(10, 5, "New Name", 2); // 10.0 -> 10.5, Vol 2
        mockMvc.perform(put("/api/v1/chapters/" + chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // Verify
        mockMvc.perform(get("/api/v1/chapters/" + chapterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayNumber", is("10.5")))
                .andExpect(jsonPath("$.name", is("New Name")));
    }

    @Test
    void shouldDeleteChapter() throws Exception {
        // Create
        var createReq = new ChapterCreateRequest(99, 0, "To Delete", null, List.of(mediaId1));
        mockMvc.perform(post("/api/v1/titles/" + titleId + "/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        var chapterId = getFirstChapterId();

        // Delete
        mockMvc.perform(delete("/api/v1/chapters/" + chapterId))
                .andExpect(status().isNoContent());

        // Verify Gone
        mockMvc.perform(get("/api/v1/chapters/" + chapterId))
                .andExpect(status().isNotFound());
    }

    private String getFirstChapterId() throws Exception {
        var chapters = mockMvc.perform(get("/api/v1/titles/" + titleId + "/chapters"))
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(chapters).get(0).get("id").asString();
    }

    private UUID createMedia(String filename) throws Exception {
        var req = new MediaUploadUrlRequest(filename, "image/jpeg", 800, 1200);
        var res = mockMvc.perform(post("/api/v1/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return jsonMapper.readValue(res.getResponse().getContentAsString(), MediaUploadUrlResponse.class).id();
    }

}
