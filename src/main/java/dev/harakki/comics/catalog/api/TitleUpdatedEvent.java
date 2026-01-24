package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleUpdatedEvent(
        UUID titleId,
        String name
) implements Serializable {
}
