package dev.harakki.comics.collections.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Response containing share link information")
public record ShareLinkResponse(

        @Schema(description = "Token used for sharing the collection", example = "abc123xyz")
        String shareToken,

        @Schema(description = "URL for sharing the collection", example = "https://example.com/...")

        String shareUrl

) implements Serializable {
}
