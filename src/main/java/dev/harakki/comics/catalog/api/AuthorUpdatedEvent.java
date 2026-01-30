package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record AuthorUpdatedEvent(
        UUID authorId,
        UUID userId
) implements Serializable {
}
