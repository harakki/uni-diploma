package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import dev.harakki.comics.catalog.infrastructure.TagMapper;
import dev.harakki.comics.catalog.infrastructure.TagRepository;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceInUseException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    private final SlugGenerator slugGenerator;

    @Transactional
    public TagResponse create(TagCreateRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Tag with name '" + request.name() + "' already exists");
        }

        var tag = tagMapper.toEntity(request);

        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = slugGenerator.generate(request.name(), tagRepository::existsBySlug);
        } else {
            if (tagRepository.existsBySlug(slug)) {
                throw new ResourceAlreadyExistsException("Tag with slug '" + slug + "' already exists");
            }
        }
        tag.setSlug(slug);

        try {
            tag = tagRepository.save(tag);
            log.info("Created tag: id={}, name={}, type={}", tag.getId(), tag.getName(), tag.getType());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Tag with this name or slug already exists");
        }

        return tagMapper.toResponse(tag);

    }

    @Transactional
    public TagResponse update(UUID id, TagUpdateRequest request) {
        var tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with id " + id + " not found"));

        if (request.slug() != null && tagRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResourceAlreadyExistsException("Tag with slug '" + request.slug() + "' already exists");
        }

        tag = tagMapper.partialUpdate(request, tag);

        tag = tagRepository.save(tag);
        log.debug("Updated tag: id={}", id);

        return tagMapper.toResponse(tag);
    }

    @Transactional
    public TagResponse updateSlug(UUID id, String slug) {
        var tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with id " + id + " not found"));

        if (tagRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResourceAlreadyExistsException("Tag with slug '" + slug + "' already exists");
        }

        tag.setSlug(slug);
        tag = tagRepository.save(tag);
        return tagMapper.toResponse(tag);
    }

    public TagResponse getById(UUID id) {
        return tagRepository.findById(id)
                .map(tagMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with id " + id + " not found"));
    }

    public TagResponse getBySlug(String slug) {
        return tagRepository.findBySlug(slug)
                .map(tagMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with slug '" + slug + "' not found"));
    }

    public Page<TagResponse> getAll(Pageable pageable) {
        return tagRepository.findAll(pageable)
                .map(tagMapper::toResponse);
    }

    @Transactional
    public void delete(UUID id) {
        var tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with id " + id + " not found"));
        try {
            tagRepository.delete(tag);
            tagRepository.flush();
            log.info("Deleted tag: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete tag with id " + id + " because it is referenced by titles");
        }
    }

}
