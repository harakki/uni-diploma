package dev.harakki.comics.collections.dto;

import java.io.Serializable;

public record ShareLinkResponse(
        String shareToken,
        String shareUrl
) implements Serializable {
}
