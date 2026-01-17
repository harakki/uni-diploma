package dev.harakki.comics.analytics.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record UserStatsResponse(
        UUID userId,
        Long totalChaptersRead,
        Long totalReadTimeMillis,
        Map<LocalDate, Integer> activityHeatmap
) implements Serializable {
}
