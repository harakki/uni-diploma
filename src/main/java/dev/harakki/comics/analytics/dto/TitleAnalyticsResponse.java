package dev.harakki.comics.analytics.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record TitleAnalyticsResponse(
        UUID titleId,
        Double averageRating,
        Long totalViews,
        Instant lastUpdated
) implements Serializable {
}
