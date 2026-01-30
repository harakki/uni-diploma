package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record PublisherUpdatedEvent(
        UUID publisherId,
        UUID userId
) implements Serializable {
}
