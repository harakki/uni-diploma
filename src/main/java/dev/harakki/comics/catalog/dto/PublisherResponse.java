package dev.harakki.comics.catalog.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.catalog.domain.Publisher}
 */
public record PublisherResponse(
        UUID id,
        String name,
        String slug,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID logoMediaId
) implements Serializable {
}
