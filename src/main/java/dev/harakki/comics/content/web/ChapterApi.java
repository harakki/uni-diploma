package dev.harakki.comics.content.web;

import dev.harakki.comics.content.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chapters", description = "Management of comic chapters.")
public interface ChapterApi {

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
    void createChapter(
            @Parameter(description = "Title UUID", required = true) UUID titleId,
            ChapterCreateRequest request
    );

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
    List<ChapterSummaryResponse> getTitleChapters(
            @Parameter(description = "Title UUID", required = true) UUID titleId
    );

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
    ChapterDetailsResponse getChapterDetails(
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId
    );

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
    void updateChapter(
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId,
            ChapterUpdateRequest request
    );

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
    void deleteChapter(
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId
    );

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
    void updatePages(
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId,
            ChapterPagesUpdateRequest request
    );

}
