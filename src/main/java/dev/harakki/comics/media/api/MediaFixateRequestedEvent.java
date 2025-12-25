package dev.harakki.comics.media.api;

import java.io.Serializable;
import java.util.UUID;

public record MediaFixateRequestedEvent(
        UUID mediaId
) implements Serializable {
}
