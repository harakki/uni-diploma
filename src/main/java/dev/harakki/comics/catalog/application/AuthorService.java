package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import dev.harakki.comics.catalog.infrastructure.AuthorMapper;
import dev.harakki.comics.catalog.infrastructure.AuthorRepository;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceInUseException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    private final SlugGenerator slugGenerator;

    @Transactional
    public AuthorResponse create(AuthorCreateRequest request) {
        if (authorRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Author with name " + request.name() + " already exists");
        }

        var author = authorMapper.toEntity(request);

        String slug = slugGenerator.generate(author.getName(), authorRepository::existsBySlug);
        author.setSlug(slug);

        try {
            author = authorRepository.save(author);
            log.info("Created author: id={}, slug={}", author.getId(), author.getSlug());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Author with this name or slug already exists");
        }

        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse update(UUID id, AuthorUpdateRequest request) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));

        author = authorMapper.partialUpdate(request, author);

        author = authorRepository.save(author);
        log.debug("Updated author: id={}", id);

        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse updateSlug(UUID id, String slug) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));

        if (authorRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResourceAlreadyExistsException("Author with slug " + slug + " already exists");
        }

        author.setSlug(slug);
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
            log.info("Deleted author: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete author with id " + id + " because it is referenced by titles");
        }
    }

}
