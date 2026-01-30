package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.ContentRating;
import dev.harakki.comics.catalog.domain.TitleStatus;
import dev.harakki.comics.catalog.domain.TitleType;

import java.time.Year;
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
        Year releaseYear,
        ContentRating contentRating,
        Boolean isLicensed,
        String countryIsoCode,
        UUID mainCoverMediaId,
        List<TitleAuthorResponse> authors,
        PublisherResponse publisher,
        Set<TagResponse> tags
) {
}
