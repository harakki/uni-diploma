package dev.harakki.comics.collections.api;

import java.io.Serializable;
import java.util.UUID;

public record CollectionCreatedEvent(
        UUID collectionId,
        UUID userId,
        String collectionName
) implements Serializable {
}
