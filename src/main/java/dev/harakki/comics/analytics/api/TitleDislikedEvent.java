package dev.harakki.comics.analytics.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleDislikedEvent(
        UUID titleId,
        UUID userId
) implements Serializable {
}
