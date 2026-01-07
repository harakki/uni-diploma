package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.AuthorRole;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.catalog.domain.TitleAuthor}
 */
public record TitleAuthorResponse(
        UUID id,
        AuthorResponse author,
        AuthorRole role,
        Integer sortOrder
) implements Serializable {
}
