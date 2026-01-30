package dev.harakki.comics.collections.api;

import java.io.Serializable;
import java.util.UUID;

public record CollectionDeletedEvent(
        UUID collectionId,
        UUID userId
) implements Serializable {
}
