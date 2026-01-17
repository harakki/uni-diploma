package dev.harakki.comics.library.application;

import dev.harakki.comics.analytics.api.TitleAddToLibraryEvent;
import dev.harakki.comics.analytics.api.TitleRatingEvent;
import dev.harakki.comics.analytics.api.TitleRemoveFromLibraryEvent;
import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.domain.ReadingStatus;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
import dev.harakki.comics.library.infrastructure.LibraryEntryMapper;
import dev.harakki.comics.library.infrastructure.LibraryEntryRepository;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import dev.harakki.comics.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryEntryService {

    private final LibraryEntryRepository libraryEntryRepository;
    private final LibraryEntryMapper libraryEntryMapper;
    private final ApplicationEventPublisher events;

    @Transactional
    public LibraryEntryResponse addToLibrary(LibraryEntryCreateRequest request) {
        UUID currentUserId = getCurrentUserId();

        if (libraryEntryRepository.existsByUserIdAndTitleId(currentUserId, request.titleId())) {
            throw new ResourceAlreadyExistsException("Title already exists in library");
        }

        var entry = libraryEntryMapper.toEntity(request);
        entry.setUserId(currentUserId);

        try {
            entry = libraryEntryRepository.save(entry);
            libraryEntryRepository.flush();

            events.publishEvent(new TitleAddToLibraryEvent(request.titleId(), currentUserId));
            log.info("Added title {} to library for user {}", request.titleId(), currentUserId);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Title already exists in library");
        }

        // If initial rating provided, publish analytics event
        if (request.rating() != null) {
            events.publishEvent(new TitleRatingEvent(request.titleId(), currentUserId, request.rating()));
            log.debug("Published TitleRatingEvent for new library entry: titleId={}, userId={}, rating={}",
                    request.titleId(), currentUserId, request.rating());
        }

        return libraryEntryMapper.toResponse(entry);
    }

    @Transactional
    public LibraryEntryResponse update(UUID entryId, LibraryEntryUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();

        var entry = libraryEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found"));

        if (!entry.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this entry");
        }

        Integer oldRating = entry.getRating();

        entry = libraryEntryMapper.partialUpdate(request, entry);
        entry = libraryEntryRepository.save(entry);

        log.debug("Updated library entry: id={}", entryId);

        // If rating changed, publish analytics event
        Integer newRating = entry.getRating();
        if (newRating != null && !newRating.equals(oldRating)) {
            events.publishEvent(new TitleRatingEvent(entry.getTitleId(), currentUserId, newRating));
            log.debug("Published TitleRatingEvent for updated library entry: titleId={}, userId={}, rating={}",
                    entry.getTitleId(), currentUserId, newRating);
        }

        return libraryEntryMapper.toResponse(entry);
    }

    @Transactional
    public void removeFromLibrary(UUID entryId) {
        UUID currentUserId = getCurrentUserId();

        var entry = libraryEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found"));

        if (!entry.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this entry");
        }

        libraryEntryRepository.delete(entry);

        events.publishEvent(new TitleRemoveFromLibraryEvent(entry.getTitleId(), currentUserId));
        log.info("Removed library entry: id={} for user {}", entryId, currentUserId);
    }

    public LibraryEntryResponse getById(UUID entryId) {
        UUID currentUserId = getCurrentUserId();

        var entry = libraryEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found"));

        if (!entry.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to view this entry");
        }

        return libraryEntryMapper.toResponse(entry);
    }

    public LibraryEntryResponse getByTitleId(UUID titleId) {
        UUID currentUserId = getCurrentUserId();

        return libraryEntryRepository.findByUserIdAndTitleId(currentUserId, titleId)
                .map(libraryEntryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found"));
    }

    public Page<LibraryEntryResponse> getMyLibrary(Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        return libraryEntryRepository.findByUserId(currentUserId, pageable)
                .map(libraryEntryMapper::toResponse);
    }

    public Page<LibraryEntryResponse> getMyLibraryByStatus(ReadingStatus status, Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        return libraryEntryRepository.findByUserIdAndStatus(currentUserId, status, pageable)
                .map(libraryEntryMapper::toResponse);
    }

    public Page<LibraryEntryResponse> getUserLibrary(UUID userId, Pageable pageable) {
        // TODO : Add privacy checks
        return libraryEntryRepository.findByUserId(userId, pageable)
                .map(libraryEntryMapper::toResponse);
    }

    public Page<LibraryEntryResponse> searchLibrary(Specification<LibraryEntry> spec, Pageable pageable) {
        UUID currentUserId = getCurrentUserId();

        // Add filter for current user
        Specification<LibraryEntry> userSpec = (root, query, cb) ->
                cb.equal(root.get("userId"), currentUserId);

        Specification<LibraryEntry> finalSpec = Specification.where(userSpec).and(spec);

        return libraryEntryRepository.findAll(finalSpec, pageable)
                .map(libraryEntryMapper::toResponse);
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
    }

}
