package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.domain.*;
import dev.harakki.comics.catalog.dto.TitleCreateRequest;
import dev.harakki.comics.catalog.dto.TitleResponse;
import dev.harakki.comics.catalog.dto.TitleUpdateRequest;
import dev.harakki.comics.catalog.infrastructure.*;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceInUseException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TitleService {

    private final TitleRepository titleRepository;
    private final PublisherRepository publisherRepository;
    private final TagRepository tagRepository;
    private final AuthorRepository authorRepository;

    private final TitleMapper titleMapper;
    private final SlugGenerator slugGenerator;

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

        try {
            title = titleRepository.save(title);
            log.info("Created title: id={}, name={}", title.getId(), title.getName());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Title with this slug already exists");
        }

        return titleMapper.toResponse(title);
    }

    @Transactional
    public TitleResponse update(UUID id, TitleUpdateRequest request) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));

        titleMapper.partialUpdate(request, title);

        // Connection with Publisher
        if (request.publisherId() != null) {
            var publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + request.publisherId() + " not found"));
            title.setPublisher(publisher);
        }

        title = titleRepository.save(title);
        log.debug("Updated title: id={}", id);

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

    public Page<TitleResponse> getAll(Pageable pageable) {
        return titleRepository.findAll(pageable)
                .map(titleMapper::toResponse);
    }

    @Transactional
    public void delete(UUID id) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));
        try {
            titleRepository.delete(title);
            log.info("Deleted title: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete title with id " + id + " because it is referenced by other resources");
        }
    }

    @Transactional
    public void addAuthor(UUID titleId, @NotNull UUID authorId, @NotNull AuthorRole role) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        boolean thisAuthorWithSameRoleExists = title.getAuthors().stream()
                .anyMatch(ta -> ta.getAuthor().getId().equals(authorId) && ta.getRole() == role);
        if (thisAuthorWithSameRoleExists) {
            throw new ResourceAlreadyExistsException("Author is already assigned to this title with role " + role);
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + authorId + " not found"));

        var titleAuthor = new TitleAuthor();
        titleAuthor.setTitle(title);
        titleAuthor.setAuthor(author);
        titleAuthor.setRole(role);
        titleAuthor.setSortOrder(title.getAuthors().size());

        title.getAuthors().add(titleAuthor);

        titleRepository.save(title);
    }

    @Transactional
    public TitleResponse updateSlug(UUID id, String slug) {
        var title = titleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + id + " not found"));

        if (titleRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResourceAlreadyExistsException("Title with slug '" + slug + "' already exists");
        }

        title.setSlug(slug);
        title = titleRepository.save(title);
        return titleMapper.toResponse(title);
    }

    @Transactional
    public void removeAuthor(UUID titleId, UUID authorId) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        boolean removed = title.getAuthors().removeIf(ta -> ta.getAuthor().getId().equals(authorId));
        if (!removed) {
            throw new ResourceNotFoundException("Author link not found for title " + titleId);
        }

        // Normalize sort order to keep authors ordered without gaps
        int sortOrder = 0;
        for (var ta : title.getAuthors()) {
            ta.setSortOrder(sortOrder++);
        }

        titleRepository.save(title);
    }

    @Transactional
    public void removePublisher(UUID titleId) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        title.setPublisher(null);

        titleRepository.save(title);
    }

    @Transactional
    public void updateTags(UUID titleId, Set<UUID> tagIds) {
        var title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title with id " + titleId + " not found"));

        if (tagIds == null || tagIds.isEmpty()) {
            title.getTags().clear();
        } else {
            Set<Tag> newTags = new HashSet<>(tagRepository.findAllById(tagIds));
            if (newTags.size() != tagIds.size()) {
                throw new ResourceNotFoundException("One or more tags not found");
            }
            title.setTags(newTags);
        }

        titleRepository.save(title);
    }

}
