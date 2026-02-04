package dev.harakki.comics.analytics.web;

import dev.harakki.comics.analytics.application.AnalyticsService;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = AnalyticsController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "Analytics API for reading statistics, ratings, and preferences.")
public class AnalyticsController {

    static final String REQUEST_MAPPING = "/api/v1/analytics";

    static final String BY_TITLE_ID = "/titles/{titleId}";

    private final AnalyticsService analyticsService;

    @Operation(
            operationId = "getTitleAnalytics",
            summary = "Get title analytics",
            description = "Retrieve analytics data for a specific title including average rating, view count, and reader count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TitleAnalyticsResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping(BY_TITLE_ID)
    public TitleAnalyticsResponse getTitleAnalytics(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId
    ) {
        return analyticsService.getTitleAnalytics(titleId);
    }

}
