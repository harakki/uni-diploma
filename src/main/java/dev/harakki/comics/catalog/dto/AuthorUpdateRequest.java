package dev.harakki.comics.catalog.dto;

import java.util.List;
import java.util.UUID;

public record AuthorUpdateRequest(
        String name,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID mainCoverMediaId
) {
}
