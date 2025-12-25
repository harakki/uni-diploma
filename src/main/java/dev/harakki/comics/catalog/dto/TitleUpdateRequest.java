package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.ContentRating;
import dev.harakki.comics.catalog.domain.TitleStatus;
import dev.harakki.comics.catalog.domain.TitleType;

import java.util.UUID;

public record TitleUpdateRequest(
        String name,
        String slug,
        String description,
        TitleType type,
        TitleStatus titleStatus,
        Short releaseYear,
        ContentRating contentRating,
        String countryIsoCode,
        UUID mainCoverMediaId
) {
}
