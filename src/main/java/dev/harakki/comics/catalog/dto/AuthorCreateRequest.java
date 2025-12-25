package dev.harakki.comics.catalog.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record AuthorCreateRequest(
        @NotBlank String name,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID mainCoverMediaId
) {
}
