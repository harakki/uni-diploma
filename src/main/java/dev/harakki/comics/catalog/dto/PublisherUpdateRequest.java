package dev.harakki.comics.catalog.dto;

import java.util.List;
import java.util.UUID;

public record PublisherUpdateRequest(
        String name,
        String slug,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID logoMediaId
) {
}
