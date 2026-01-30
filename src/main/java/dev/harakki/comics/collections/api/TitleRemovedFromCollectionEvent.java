package dev.harakki.comics.collections.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleRemovedFromCollectionEvent(
        UUID collectionId,
        UUID titleId,
        UUID userId
) implements Serializable {
}
