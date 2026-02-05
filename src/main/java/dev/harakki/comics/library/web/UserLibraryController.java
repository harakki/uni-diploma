package dev.harakki.comics.library.web;

import dev.harakki.comics.library.application.LibraryService;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/users/{userId}/library", produces = MediaType.APPLICATION_JSON_VALUE)
class UserLibraryController implements UserLibraryApi {

    private final LibraryService libraryService;

    @GetMapping
    public Page<LibraryEntryResponse> getUserLibrary(
            @PathVariable UUID userId,
            @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryService.getUserLibrary(userId, pageable);
    }

}
