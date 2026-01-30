package dev.harakki.comics.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Analytics data for a specific title")
public record TitleAnalyticsResponse(

        @Schema(description = "Title unique identifier", example = "019b9d1e-bc3a-70f3-8520-36e8d82dc9e0")
        UUID titleId,

        @Schema(description = "Average rating of the title", example = "4.5")
        Double averageRating,

        @Schema(description = "Number of unique readers", example = "997")
        Long totalViews,

        @Schema(description = "Last updated timestamp", example = "2024-05-01T12:34:56Z")
        Instant lastUpdated

) implements Serializable {
}
