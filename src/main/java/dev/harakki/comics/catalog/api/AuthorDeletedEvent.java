package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record AuthorDeletedEvent(
        UUID authorId,
        UUID userId
) implements Serializable {
}
