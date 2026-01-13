package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Page;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Page}
 */
public record PageResponse(
        UUID id,
        UUID mediaId,
        String url,
        Integer pageOrder
) implements Serializable {
}
