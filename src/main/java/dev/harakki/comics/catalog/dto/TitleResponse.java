package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.catalog.domain.Title}
 */
public record TitleResponse(
        UUID id,
        String name,
        String slug,
        String description,
        TitleType type,
        TitleStatus titleStatus,
        Short releaseYear,
        ContentRating contentRating,
        Boolean isLicensed,
        String countryIsoCode,
        UUID mainCoverMediaId,
        List<TitleAuthor> authors,
        Publisher publisher,
        Set<Tag> tags
) {
}
