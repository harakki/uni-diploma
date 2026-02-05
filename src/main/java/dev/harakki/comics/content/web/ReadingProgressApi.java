package dev.harakki.comics.content.web;

import dev.harakki.comics.content.dto.ChapterReadRequest;
import dev.harakki.comics.content.dto.ChapterReadStatusResponse;
import dev.harakki.comics.content.dto.NextChapterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Reading progress", description = "Management of user reading progress.")
public interface ReadingProgressApi {

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
    void recordChapterRead(
            @Parameter(description = "Title UUID", required = true) UUID titleId,
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId,
            ChapterReadRequest request
    );

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
    ChapterReadStatusResponse isChapterRead(
            @Parameter(description = "Title UUID", required = true) UUID titleId,
            @Parameter(description = "Chapter UUID", required = true) UUID chapterId
    );

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
    NextChapterResponse getNextUnreadChapter(
            @Parameter(description = "Title UUID", required = true) UUID titleId
    );

}
