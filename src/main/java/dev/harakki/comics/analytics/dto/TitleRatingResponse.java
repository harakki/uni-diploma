package dev.harakki.comics.analytics.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.analytics.domain.TitleRating}
 */
public record TitleRatingResponse(
        UUID id,
        UUID titleId,
        UUID userId,
        Integer rating,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
