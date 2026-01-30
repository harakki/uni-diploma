package dev.harakki.comics.collections.api;

import java.io.Serializable;
import java.util.UUID;

public record CollectionUpdatedEvent(
        UUID collectionId,
        UUID userId
) implements Serializable {
}
