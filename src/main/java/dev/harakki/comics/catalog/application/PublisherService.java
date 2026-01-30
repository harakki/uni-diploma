package dev.harakki.comics.catalog.application;

import dev.harakki.comics.catalog.api.PublisherCreatedEvent;
import dev.harakki.comics.catalog.api.PublisherDeletedEvent;
import dev.harakki.comics.catalog.api.PublisherUpdatedEvent;
import dev.harakki.comics.catalog.domain.Publisher;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.catalog.infrastructure.PublisherMapper;
import dev.harakki.comics.catalog.infrastructure.PublisherRepository;
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
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    private final SlugGenerator slugGenerator;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PublisherResponse create(PublisherCreateRequest request) {
        if (publisherRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Publisher with name '" + request.name() + "' already exists");
        }

        var publisher = publisherMapper.toEntity(request);

        // Generate and set unique slug
        String slug = slugGenerator.generate(publisher.getName(), publisherRepository::existsBySlug);
        publisher.setSlug(slug);

        if (request.logoMediaId() != null) {
            eventPublisher.publishEvent(new MediaFixateRequestedEvent(request.logoMediaId()));
        }

        try {
            publisher = publisherRepository.save(publisher);
            log.info("Created publisher: id={}, slug={}", publisher.getId(), publisher.getSlug());

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new PublisherCreatedEvent(publisher.getId(), userId, publisher.getName()));
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Publisher with this name or slug already exists");
        }

        return publisherMapper.toResponse(publisher);
    }

    @Transactional
    public PublisherResponse update(UUID id, PublisherUpdateRequest request) {
        var publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + id + " not found"));

        if (request.name() != null && publisherRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ResourceAlreadyExistsException("Publisher with name '" + request.name() + "' already exists");
        }

        var oldMediaId = publisher.getLogoMediaId();

        publisher = publisherMapper.partialUpdate(request, publisher);
        var newMediaId = publisher.getLogoMediaId();

        if (!Objects.equals(oldMediaId, newMediaId)) {
            if (newMediaId != null) {
                eventPublisher.publishEvent(new MediaFixateRequestedEvent(newMediaId));
            }
            if (oldMediaId != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(oldMediaId));
            }
        }

        publisher = publisherRepository.save(publisher);
        log.debug("Updated publisher: id={}", id);

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new PublisherUpdatedEvent(publisher.getId(), userId));
        }

        return publisherMapper.toResponse(publisher);
    }

    @Transactional
    public PublisherResponse updateSlug(UUID id, @Valid ReplaceSlugRequest request) {
        var publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + id + " not found"));

        if (publisherRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResourceAlreadyExistsException("Publisher with slug '" + request + "' already exists");
        }

        publisher.setSlug(request.slug());
        publisher = publisherRepository.save(publisher);
        return publisherMapper.toResponse(publisher);
    }


    public PublisherResponse getById(UUID id) {
        return publisherRepository.findById(id)
                .map(publisherMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + id + " not found"));
    }

    public PublisherResponse getBySlug(String slug) {
        return publisherRepository.findBySlug(slug)
                .map(publisherMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher with slug '" + slug + "' not found"));
    }


    public Page<PublisherResponse> getAll(Specification<Publisher> spec, Pageable pageable) {
        return publisherRepository.findAll(spec, pageable)
                .map(publisherMapper::toResponse);
    }

    @Transactional
    public void delete(UUID id) {
        var publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher with id " + id + " not found"));
        try {
            publisherRepository.delete(publisher);
            publisherRepository.flush();

            if (publisher.getLogoMediaId() != null) {
                eventPublisher.publishEvent(new MediaDeleteRequestedEvent(publisher.getLogoMediaId()));
            }

            // Publish analytics event
            var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
            if (userId != null) {
                eventPublisher.publishEvent(new PublisherDeletedEvent(publisher.getId(), userId));
            }

            log.info("Deleted publisher: id={}", id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete publisher with id " + id + " because it is referenced by titles");
        }
    }

}
