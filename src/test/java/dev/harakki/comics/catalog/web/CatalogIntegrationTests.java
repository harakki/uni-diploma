package dev.harakki.comics.catalog.web;

import dev.harakki.comics.BaseIntegrationTest;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.harakki.comics.BaseIntegrationTest;
import dev.harakki.comics.catalog.domain.AuthorRole;
import dev.harakki.comics.catalog.domain.ContentRating;
import dev.harakki.comics.catalog.domain.TagType;
import dev.harakki.comics.catalog.domain.TitleStatus;
import dev.harakki.comics.catalog.domain.TitleType;
import dev.harakki.comics.catalog.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class CatalogIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldCreateAndGetAuthor() throws Exception {
        var request = new AuthorCreateRequest("Tatsuki Fujimoto", "Modern genius", List.of(), "JP", null);

        // Create
        var result = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Tatsuki Fujimoto")))
                .andExpect(jsonPath("$.slug", notNullValue())) // Slug generated automatically
                .andReturn();

        var response = jsonMapper.readValue(result.getResponse().getContentAsString(), AuthorResponse.class);

        // Get by ID
        mockMvc.perform(get("/api/v1/authors/" + response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Tatsuki Fujimoto")));
    }

    @Test
    void shouldFailOnDuplicatePublisher() throws Exception {
        var request = new PublisherCreateRequest("Shueisha", "Jump", List.of(), "JP", null);

        // First create
        mockMvc.perform(post("/api/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second create -> 409 Conflict
        mockMvc.perform(post("/api/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("already exists")));
    }

    @Test
    void shouldManageTags() throws Exception {
        var request = new TagCreateRequest("Action", "action-manga", TagType.GENRE, "Boom-boom!");

        // Create
        var createResult = mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var tag = jsonMapper.readValue(createResult.getResponse().getContentAsString(), TagResponse.class);

        // Update
        var updateRequest = new TagUpdateRequest("Super Action", null, TagType.GENRE, null);
        mockMvc.perform(put("/api/v1/tags/" + tag.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Super Action")));
    }

    @Test
    void shouldCreateTitleWithRelations() throws Exception {
        // Prepare deps (Author, Publisher, Tag)
        var authorRes = createAuthor("ONE");
        var publisherRes = createPublisher("Kodansha");
        var tagRes = createTag("Comedy");

        // Create Title Request
        var titleRequest = new TitleCreateRequest(
                "One Punch Man",
                "Hero for fun",
                TitleType.MANGA,
                TitleStatus.ONGOING,
                Year.of(2012),
                ContentRating.SAFE,
                "JP",
                null,
                Map.of(authorRes.id(), AuthorRole.STORY_AND_ART),
                publisherRes.id(),
                Set.of(tagRes.id())
        );

        // Perform Post
        mockMvc.perform(post("/api/v1/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(titleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("One Punch Man")))
                .andExpect(jsonPath("$.publisher.name", is("Kodansha")))
                .andExpect(jsonPath("$.authors[0].author.name", is("ONE")))
                .andExpect(jsonPath("$.tags[0].name", is("Comedy")));
    }

    @Test
    void shouldFilterTitles() throws Exception {
        var publisher = createPublisher("Shonen Jump");
        var tag = createTag("Horror");

        createTitle("Chainsaw Man", TitleType.MANGA, TitleStatus.ONGOING, 2018, publisher.id(), Set.of(tag.id()));
        createTitle("Naruto", TitleType.MANGA, TitleStatus.COMPLETED, 1999, publisher.id(), Set.of());
        createTitle("Solo Leveling", TitleType.MANHWA, TitleStatus.COMPLETED, 2018, null, Set.of());

        // Search by name (partial, case-insensitive)
        mockMvc.perform(get("/api/v1/titles?search=chain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Chainsaw Man")));

        // Filter by yearFrom and type
        mockMvc.perform(get("/api/v1/titles?yearFrom=2018&type=MANHWA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Solo Leveling")));

        // Filter by tag
        mockMvc.perform(get("/api/v1/titles?tags=horror"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Chainsaw Man")));
    }

    @Test
    void shouldReturn404ForNonExistentResource() throws Exception {
        mockMvc.perform(get("/api/v1/authors/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }

    @Test
    void shouldFailValidationOnBadData() throws Exception {
        var request = new AuthorCreateRequest("", null, null, "USA", null);

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name", notNullValue()))
                .andExpect(jsonPath("$.errors.countryIsoCode", notNullValue()));
    }

    @Test
    void shouldUpdateAuthorDetailsAndSlug() throws Exception {
        // Create
        var author = createAuthor("Old Name");

        // Update Details
        var updateReq = new AuthorUpdateRequest("New Name", "New Desc", null, "US", null);
        mockMvc.perform(put("/api/v1/authors/" + author.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.countryIsoCode", is("US")));

        // Update Slug
        mockMvc.perform(put("/api/v1/authors/" + author.id() + "/slug")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("new-slug-name")) // String body directly
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is("new-slug-name")));

        // Verify Get by new Slug
        mockMvc.perform(get("/api/v1/authors/slug/new-slug-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(author.id().toString())));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        var author = createAuthor("To Delete");

        mockMvc.perform(delete("/api/v1/authors/" + author.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/authors/" + author.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldManagePublisherLifecycle() throws Exception {
        var publisher = createPublisher("Old Pub");

        // Update
        var updateReq = new PublisherUpdateRequest("New Pub", null, null, "UK", null);
        mockMvc.perform(put("/api/v1/publishers/" + publisher.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Pub")));

        // Slug Update
        mockMvc.perform(put("/api/v1/publishers/" + publisher.id() + "/slug")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("brand-new-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is("brand-new-slug")));

        // Delete
        mockMvc.perform(delete("/api/v1/publishers/" + publisher.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateTitle() throws Exception {
        var publisher = createPublisher("P1");
        var title = createTitle("Original Title", TitleType.MANGA, TitleStatus.ONGOING, 2020, publisher.id(), Set.of());

        // Update
        var updateReq = new TitleUpdateRequest("Updated Title", "New Desc", TitleType.NOVEL, TitleStatus.COMPLETED, Year.of(2021), ContentRating.EROTICA, "KR", null, publisher.id());

        mockMvc.perform(put("/api/v1/titles/" + title.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Title")))
                .andExpect(jsonPath("$.type", is("NOVEL")));

        // Replace Slug
        var slugReq = new ReplaceSlugRequest("custom-title-slug");
        mockMvc.perform(put("/api/v1/titles/" + title.id() + "/slug")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(slugReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is("custom-title-slug")));
    }

    @Test
    void shouldManageTitleRelations() throws Exception {
        // Setup
        var title = createTitle("Relations Title", TitleType.COMIC, TitleStatus.ONGOING, 2022, null, Set.of());
        var author = createAuthor("Artist A");
        var tag = createTag("Fantasy");

        // Add Author
        var addAuthorReq = new TitleAddAuthorRequest(author.id(), AuthorRole.ART);
        mockMvc.perform(post("/api/v1/titles/" + title.id() + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(addAuthorReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authors", hasSize(1)));

        // Add Same Author (Conflict check)
        mockMvc.perform(post("/api/v1/titles/" + title.id() + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(addAuthorReq)))
                .andExpect(status().isConflict()); // ResourceAlreadyExists

        // Remove Author
        mockMvc.perform(delete("/api/v1/titles/" + title.id() + "/authors/" + author.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authors", hasSize(0)));

        // Update Tags (Replace)
        var tagsReq = new ReplaceTagsRequest(Set.of(tag.id()));
        mockMvc.perform(post("/api/v1/titles/" + title.id() + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)));

        // Clear Tags
        var emptyTagsReq = new ReplaceTagsRequest(Set.of()); // Empty set -> clear
        var tag2 = createTag("Sci-Fi");
        var tagsReq2 = new ReplaceTagsRequest(Set.of(tag2.id()));

        mockMvc.perform(post("/api/v1/titles/" + title.id() + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(tagsReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].name", is("Sci-Fi")));
    }

    @Test
    void shouldReturnBadRequestOnInvalidSort() throws Exception {
        mockMvc.perform(get("/api/v1/titles?sort=invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid Sorting Property")));
    }

    private AuthorResponse createAuthor(String name) throws Exception {
        var result = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new AuthorCreateRequest(name, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return jsonMapper.readValue(result.getResponse().getContentAsString(), AuthorResponse.class);
    }

    private PublisherResponse createPublisher(String name) throws Exception {
        var result = mockMvc.perform(post("/api/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new PublisherCreateRequest(name, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return jsonMapper.readValue(result.getResponse().getContentAsString(), PublisherResponse.class);
    }

    private TagResponse createTag(String name) throws Exception {
        var result = mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new TagCreateRequest(name, null, TagType.GENRE, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return jsonMapper.readValue(result.getResponse().getContentAsString(), TagResponse.class);
    }

    private TitleResponse createTitle(String name, TitleType type, TitleStatus status, int releaseYear, UUID publisherId, Set<UUID> tagIds) throws Exception {
        var request = new TitleCreateRequest(
                name,
                "Description for " + name,
                type,
                status,
                Year.of(releaseYear),
                ContentRating.SAFE,
                "JP",
                null,
                null,
                publisherId,
                tagIds
        );

        var result = mockMvc.perform(post("/api/v1/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return jsonMapper.readValue(result.getResponse().getContentAsString(), TitleResponse.class);
    }

}
