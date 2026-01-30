package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.api.TitleCreatedEvent;
import dev.harakki.comics.catalog.api.TitleDeletedEvent;
import dev.harakki.comics.catalog.api.TitleUpdatedEvent;
import dev.harakki.comics.catalog.domain.*;
import dev.harakki.comics.catalog.dto.*;
import dev.harakki.comics.catalog.infrastructure.*;
import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaFixateRequestedEvent;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceInUseException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import dev.harakki.comics.shared.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

import java.util.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TitleService {

    private final TitleRepository titleRepository;
    private final PublisherRepository publisherRepository;
    private final TagRepository tagRepository;
    private final AuthorRepository authorRepository;
    private final TitleAuthorRepository titleAuthorRepository;

    private final TitleMapper titleMapper;
    private final SlugGenerator slugGenerator;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TitleResponse create(TitleCreateRequest request) {
        if (titleRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Title with name '" + request.name() + "' already exists");
        }

        var title = titleMapper.toEntity(request);

        var slug = slugGenerator.generate(request.name(), titleRepository::existsBySlug);
        title.setSlug(slug);

        // Connection with Authors
        if (request.authorIds() != null && !request.authorIds().isEmpty()) {
            Set<UUID> authorIds = request.authorIds().keySet();

            List<Author> authors = authorRepository.findAllById(authorIds);
            if (authors.size() != authorIds.size()) {
                throw new ResourceNotFoundException("One or more authors not found");
            }

            int sortOrder = 0;

            for (var author : authors) {
                var role = request.authorIds().get(author.getId());

                var titleAuthor = new TitleAuthor();
                titleAuthor.setTitle(title);  // Bind to title
                titleAuthor.setAuthor(author); // Bind to author
                titleAuthor.setRole(role);
                titleAuthor.setSortOrder(sortOrder++); // 0, 1, 2...

                // Add to title's collection
                title.getAuthors().add(titleAuthor);
            }
        }

        // Connection with Publisher
        if (request.publisherId() != null) {
            var publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + request.publisherId() + " not found"));
            title.setPublisher(publisher);
        }

        // Connection with Tags
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.tagIds()));
            if (tags.size() != request.tagIds().size()) {
                throw new ResourceNotFoundException("Some tags were not found");
            }
            title.setTags(tags);
        }

        // Media Fixation
        if (request.mainCoverMediaId() != null) {
            eventPublisher.publishEvent(new MediaFixateRequestedEvent(request.mainCoverMediaId()));
        }

        try {
            title = titleRepository.save(title);
            log.info("Created title: id={}, name={}", title.getId(), title.getName());

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new TitleCreatedEvent(title.getId(), userId, title.getName()));
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Title with this slug already exists");
        }

        return titleMapper.toResponse(title);
    }

    @Transactional
    public TitleResponse update(UUID id, TitleUpdateRequest request) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));

        var oldMediaId = title.getMainCoverMediaId();

        title = titleMapper.partialUpdate(request, title);

        // Connection with Publisher
        if (request.publisherId() != null) {
            var publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + request.publisherId() + " not found"));
            title.setPublisher(publisher);
        }

        var newMediaId = title.getMainCoverMediaId();

        if (!Objects.equals(oldMediaId, newMediaId)) {
            if (newMediaId != null) {
                eventPublisher.publishEvent(new MediaFixateRequestedEvent(newMediaId));
            }
            if (oldMediaId != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(oldMediaId));
            }
        }

        title = titleRepository.save(title);
        log.debug("Updated title: id={}", id);

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new TitleUpdatedEvent(title.getId(), userId));
        }

        return titleMapper.toResponse(title);
    }

    public TitleResponse getById(UUID id) {
        return titleRepository.findById(id)
                .map(titleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));
    }

    public TitleResponse getBySlug(String slug) {
        return titleRepository.findBySlug(slug)
                .map(titleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Title with slug '" + slug + "' not found"));
    }

    public Page<TitleResponse> getAll(Specification<Title> spec, Pageable pageable) {
        return titleRepository.findAll(spec, pageable)
                .map(titleMapper::toResponse);
    }

    @Transactional
    public void delete(UUID id) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));
        try {
            titleRepository.delete(title);
            titleRepository.flush();

            if (title.getMainCoverMediaId() != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(title.getMainCoverMediaId()));
            }

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new TitleDeletedEvent(title.getId(), userId));
            }

            log.info("Deleted title: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete title with id " + id + " because it is referenced by other resources");
        }
    }

    @Transactional
    public TitleResponse addAuthor(UUID titleId, @NotNull UUID authorId, @NotNull AuthorRole role) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        boolean thisAuthorWithSameRoleExists = title.getAuthors().stream()
                .anyMatch(ta -> ta.getAuthor().getId().equals(authorId) && ta.getRole() == role);
        if (thisAuthorWithSameRoleExists) {
            throw new ResourceAlreadyExistsException("Author is already assigned to this title with role " + role);
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + authorId + " not found"));

        int nextSortOrder = title.getAuthors().stream()
                .mapToInt(TitleAuthor::getSortOrder)
                .max()
                .orElse(-1) + 1;

        var titleAuthor = new TitleAuthor();
        titleAuthor.setTitle(title);
        titleAuthor.setAuthor(author);
        titleAuthor.setRole(role);
        titleAuthor.setSortOrder(nextSortOrder);

        title.getAuthors().add(titleAuthor);

        return titleMapper.toResponse(titleRepository.save(title));
    }

    @Transactional
    public TitleResponse updateSlug(UUID id, @Valid ReplaceSlugRequest request) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));

        if (titleRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResourceAlreadyExistsException("Title with slug '" + request.slug() + "' already exists");
        }

        title.setSlug(request.slug());
        title = titleRepository.save(title);

        return titleMapper.toResponse(title);
    }

    @Transactional
    public TitleResponse removeAuthor(UUID titleId, UUID authorId) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        boolean removed = title.getAuthors().removeIf(ta -> ta.getAuthor().getId().equals(authorId));
        if (!removed) {
            throw new ResourceNotFoundException("Author link not found for title " + titleId);
        }

        // Normalize sort order
        int sortOrder = 0;
        for (var ta : title.getAuthors()) {
            ta.setSortOrder(sortOrder++);
        }

        return titleMapper.toResponse(titleRepository.save(title));
    }

    @Transactional
    public TitleResponse removePublisher(UUID titleId) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        title.setPublisher(null);

        return titleMapper.toResponse(titleRepository.save(title));
    }

    @Transactional
    public TitleResponse updateTags(UUID titleId, @Valid ReplaceTagsRequest request) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        if (request.tagIds() == null || request.tagIds().isEmpty()) {
            title.getTags().clear();
        } else {
            Set<Tag> newTags = new HashSet<>(tagRepository.findAllById(request.tagIds()));
            if (newTags.size() != request.tagIds().size()) {
                throw new ResourceNotFoundException("One or more tags not found");
            }
            title.setTags(newTags);
        }

        return titleMapper.toResponse(titleRepository.save(title));
    }

}
