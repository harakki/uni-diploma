package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record PublisherCreatedEvent(
        UUID publisherId,
        UUID userId,
        String publisherName
) implements Serializable {
}
