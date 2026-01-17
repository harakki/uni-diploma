package dev.harakki.comics.analytics.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.analytics.domain.TitleView}
 */
public record TitleViewResponse(
        UUID id,
        UUID titleId,
        UUID userId,
        Long viewCount,
        Instant createdAt
) implements Serializable {
}
