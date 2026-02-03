package dev.harakki.comics.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PublisherCreateRequest(
        @NotBlank String name,
        String description,
        List<String> websiteUrls,
        String countryIsoCode,
        UUID logoMediaId
) {
}
