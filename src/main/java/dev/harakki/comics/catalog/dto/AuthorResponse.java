package dev.harakki.comics.catalog.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.catalog.domain.Author}
 */
public record AuthorResponse(
        UUID id,
        String name,
        String slug,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID mainCoverMediaId
) {
}
