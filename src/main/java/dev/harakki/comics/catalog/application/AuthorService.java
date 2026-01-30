package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.api.AuthorCreatedEvent;
import dev.harakki.comics.catalog.api.AuthorDeletedEvent;
import dev.harakki.comics.catalog.api.AuthorUpdatedEvent;
import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.catalog.infrastructure.AuthorMapper;
import dev.harakki.comics.catalog.infrastructure.AuthorRepository;
import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaFixateRequestedEvent;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceInUseException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import dev.harakki.comics.shared.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    private final SlugGenerator slugGenerator;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthorResponse create(AuthorCreateRequest request) {
        if (authorRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Author with name " + request.name() + " already exists");
        }

        var author = authorMapper.toEntity(request);

        // Generate and set unique slug
        String slug = slugGenerator.generate(author.getName(), authorRepository::existsBySlug);
        author.setSlug(slug);

        if (request.mainCoverMediaId() != null) {
            eventPublisher.publishEvent(new MediaFixateRequestedEvent(request.mainCoverMediaId()));
        }

        try {
            author = authorRepository.save(author);
            log.info("Created author: id={}, slug={}", author.getId(), author.getSlug());

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new AuthorCreatedEvent(author.getId(), userId, author.getName()));
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Author with this name or slug already exists");
        }

        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse update(UUID id, AuthorUpdateRequest request) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));

        var oldMediaId = author.getMainCoverMediaId();

        author = authorMapper.partialUpdate(request, author);
        var newMediaId = author.getMainCoverMediaId();

        if (!Objects.equals(oldMediaId, newMediaId)) {
            if (newMediaId != null) {
                eventPublisher.publishEvent(new MediaFixateRequestedEvent(newMediaId));
            }
            if (oldMediaId != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(oldMediaId));
            }
        }

        author = authorRepository.save(author);
        log.debug("Updated author: id={}", id);

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new AuthorUpdatedEvent(author.getId(), userId));
        }

        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse updateSlug(UUID id, @Valid ReplaceSlugRequest request) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));

        if (authorRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResourceAlreadyExistsException("Author with slug " + request.slug() + " already exists");
        }

        author.setSlug(request.slug());
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    public AuthorResponse getById(UUID id) {
        return authorRepository.findById(id)
                .map(authorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));

    }

    public AuthorResponse getBySlug(String slug) {
        return authorRepository.findBySlug(slug)
                .map(authorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author with slug " + slug + " not found"));
    }


    public Page<AuthorResponse> getAll(Specification<Author> spec, Pageable pageable) {
        return authorRepository.findAll(spec, pageable)
                .map(authorMapper::toResponse);
    }

    @Transactional
    public void delete(UUID id) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        try {
            authorRepository.delete(author);
            authorRepository.flush();

            if (author.getMainCoverMediaId() != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(author.getMainCoverMediaId()));
            }

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new AuthorDeletedEvent(author.getId(), userId));
            }

            log.info("Deleted author: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete author with id " + id + " because it is referenced by titles");
        }
    }

}
