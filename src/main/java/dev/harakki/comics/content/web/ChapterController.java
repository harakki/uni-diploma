package dev.harakki.comics.content.web;

import dev.harakki.comics.content.application.ChapterService;
import dev.harakki.comics.content.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Chapters", description = "Management of comic chapters.")
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping("/titles/{titleId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            operationId = "createChapter",
            summary = "Create chapter",
            description = "Create a chapter and link uploaded pages to it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chapter created"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void createChapter(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId,
            @RequestBody @Valid ChapterCreateRequest request
    ) {
        chapterService.create(titleId, request);
    }

    @GetMapping("/titles/{titleId}/chapters")
    @Operation(
            operationId = "getTitleChapters",
            summary = "Get title chapters",
            description = "List all chapters for a title (without pages)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chapters retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChapterSummaryResponse.class)))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public List<ChapterSummaryResponse> getTitleChapters(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId
    ) {
        return chapterService.getChaptersByTitle(titleId);
    }

    @GetMapping("/chapters/{chapterId}")
    @Operation(
            operationId = "getChapterDetails",
            summary = "Get specific chapter content",
            description = "Get chapter metadata and all page URLs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chapter details",
                    content = @Content(schema = @Schema(implementation = ChapterDetailsResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ChapterDetailsResponse getChapterDetails(
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId
    ) {
        return chapterService.getChapterDetails(chapterId);
    }

    @PutMapping("/chapters/{chapterId}")
    @Operation(
            operationId = "updateChapter",
            summary = "Update chapter info",
            description = "Update number, name or volume."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chapter updated"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void updateChapter(
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterUpdateRequest request
    ) {
        chapterService.updateMetadata(chapterId, request);
    }

    @DeleteMapping("/chapters/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteChapter",
            summary = "Delete chapter",
            description = "Delete chapter and all associated pages."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chapter deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void deleteChapter(
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId
    ) {
        chapterService.delete(chapterId);
    }

    @PutMapping("/chapters/{chapterId}/pages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "updateChapterPages",
            summary = "Update pages order/content",
            description = "Full replacement of pages list. Used for reordering, adding or deleting pages for a chapter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pages updated"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void updatePages(
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterPagesUpdateRequest request
    ) {
        chapterService.updatePages(chapterId, request.pages());
    }

    @PostMapping("/titles/{titleId}/chapters/{chapterId}/read")
    @Operation(
            operationId = "recordChapterRead",
            summary = "Record chapter read",
            description = "Record that a user has read a chapter and track read time for analytics."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Read recorded"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void recordChapterRead(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId,
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterReadRequest request
    ) {
        chapterService.recordChapterRead(chapterId, titleId, request);
    }

    @GetMapping("/titles/{titleId}/chapters/{chapterId}/read")
    @Operation(
            operationId = "isChapterRead",
            summary = "Check if chapter is read",
            description = "Check if the authenticated user has read the specified chapter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Read status",
                    content = @Content(schema = @Schema(implementation = ChapterReadStatusResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ChapterReadStatusResponse isChapterRead(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId,
            @Parameter(description = "Chapter UUID", required = true)
            @PathVariable UUID chapterId,
            @Parameter(description = "User UUID", required = true)
            @RequestParam UUID userId
    ) {
        return chapterService.isChapterRead(chapterId, titleId, userId);
    }

    @GetMapping("/users/{userId}/titles/{titleId}/next-chapter")
    @Operation(
            operationId = "getNextUnreadChapter",
            summary = "Get next unread chapter",
            description = "Get the next unread chapter for a user in a specific title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next chapter",
                    content = @Content(schema = @Schema(implementation = NextChapterResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public NextChapterResponse getNextUnreadChapter(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId
    ) {
        return chapterService.getNextUnreadChapter(userId, titleId);
    }

}
