package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.TagType;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.catalog.domain.Tag}
 */
public record TagResponse(
        UUID id,
        String name,
        String slug,
        TagType type,
        String description
) implements Serializable {
}
