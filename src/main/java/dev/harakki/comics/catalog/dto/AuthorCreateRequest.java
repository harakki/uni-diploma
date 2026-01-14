package dev.harakki.comics.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AuthorCreateRequest(
        @NotBlank String name,
        String description,
        List<String> websiteUrls,
        @Size(min = 2, max = 2) String countryIsoCode,
        UUID mainCoverMediaId
) {
}
