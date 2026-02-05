package dev.harakki.comics.analytics.web;

import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Analytics", description = "Analytics API for interaction statistics.")
public interface AnalyticsApi {

    @Operation(
            operationId = "getTitleAnalytics",
            summary = "Get title analytics",
            description = "Retrieve analytics data for a specific title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TitleAnalyticsResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleAnalyticsResponse getTitleAnalytics(@Parameter(description = "Title UUID", required = true) UUID titleId);

}
