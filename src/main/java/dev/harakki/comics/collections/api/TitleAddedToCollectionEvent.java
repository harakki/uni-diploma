package dev.harakki.comics.collections.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleAddedToCollectionEvent(
        UUID collectionId,
        UUID titleId,
        UUID userId
) implements Serializable {
}
