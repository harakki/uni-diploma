package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.TagType;

public record TagUpdateRequest(
        String name,
        String slug,
        TagType type,
        String description
) {
}
