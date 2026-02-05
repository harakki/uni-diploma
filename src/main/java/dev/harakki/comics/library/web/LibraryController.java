package dev.harakki.comics.library.web;

import dev.harakki.comics.library.application.LibraryService;
import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping(path = "/api/v1/library", produces = MediaType.APPLICATION_JSON_VALUE)
class LibraryController implements LibraryApi {

    private final LibraryService libraryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEntryResponse addToLibrary(@RequestBody @Valid LibraryEntryCreateRequest request) {
        return libraryService.addToLibrary(request);
    }

    @GetMapping("/{entryId}")
    public LibraryEntryResponse getEntry(@PathVariable @NotNull UUID entryId) {
        return libraryService.getById(entryId);
    }

    @GetMapping
    public Page<LibraryEntryResponse> getMyLibrary(
            @And({
                    @Spec(path = "status", params = "status", spec = Equal.class),
                    @Spec(path = "title.id", params = "titleId", spec = Equal.class)
            }) Specification<LibraryEntry> spec,
            @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryService.searchLibrary(spec, pageable);
    }

    @PutMapping("/{entryId}")
    public LibraryEntryResponse updateEntry(
            @PathVariable UUID entryId,
            @RequestBody @Valid LibraryEntryUpdateRequest request
    ) {
        return libraryService.update(entryId, request);
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromLibrary(@PathVariable UUID entryId) {
        libraryService.removeFromLibrary(entryId);
    }

}
