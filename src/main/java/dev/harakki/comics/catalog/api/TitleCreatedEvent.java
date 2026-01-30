package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleCreatedEvent(
        UUID titleId,
        UUID userId,
        String titleName
) implements Serializable {
}
