package dev.harakki.comics.catalog.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleViewedEvent(
        UUID titleId,
        UUID userId
) implements Serializable {
}
