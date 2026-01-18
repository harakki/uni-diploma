package dev.harakki.comics.analytics.web;

import dev.harakki.comics.analytics.application.AnalyticsService;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "Analytics API for reading statistics, ratings, and preferences.")
@ApiProblemResponses
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/titles/{titleId}")
    @Operation(summary = "Get title analytics", description = "Retrieve analytics data for a specific title including average rating, view count, and reader count.")
    @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully")
    public TitleAnalyticsResponse getTitleAnalytics(@PathVariable UUID titleId) {
        Double averageRating = analyticsService.getAverageRatingForTitle(titleId);
        Long totalViews = analyticsService.getTotalViewCount(titleId);

        return new TitleAnalyticsResponse(
                titleId,
                averageRating,
                totalViews != null ? totalViews : 0L,
                Instant.now()
        );
    }

}
