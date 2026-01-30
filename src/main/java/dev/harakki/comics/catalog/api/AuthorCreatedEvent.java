package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record AuthorCreatedEvent(
        UUID authorId,
        UUID userId,
        String authorName
) implements Serializable {
}
