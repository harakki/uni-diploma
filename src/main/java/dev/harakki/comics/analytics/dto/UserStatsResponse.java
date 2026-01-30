package dev.harakki.comics.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Schema(description = "User reading statistics and activity heatmap")
public record UserStatsResponse(

        @Schema(description = "User unique identifier", example = "019b9d1e-bc3a-70f3-8520-36e8d82dc9e0")
        UUID userId,

        @Schema(description = "Total number of chapters read by the user", example = "150")
        Long totalChaptersRead,

        @Schema(description = "Total time spent reading in milliseconds", example = "3601111")
        Long totalReadTimeMillis,

        @Schema(description = "Activity heatmap showing number of chapters read per day",
                example = "{\"2026-01-28\": 3, \"2026-01-29\": 5, \"2026-01-30\": 0}")
        Map<LocalDate, Integer> activityHeatmap

) implements Serializable {
}
