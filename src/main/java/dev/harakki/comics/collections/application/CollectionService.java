package dev.harakki.comics.collections.application;

import dev.harakki.comics.collections.api.*;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
import dev.harakki.comics.collections.infrastructure.CollectionMapper;
import dev.harakki.comics.collections.infrastructure.CollectionRepository;
import dev.harakki.comics.shared.exception.ResourceAlreadyExistsException;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import dev.harakki.comics.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserCollectionResponse create(CollectionCreateRequest request) {
        UUID currentUserId = getCurrentUserId();

        if (collectionRepository.existsByAuthorIdAndName(currentUserId, request.name())) {
            throw new ResourceAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
        }

        var entity = collectionMapper.toEntity(request);
        entity.setAuthorId(currentUserId);

        try {
            entity = collectionRepository.save(entity);
            collectionRepository.flush();
            log.info("Created collection {} by user {}", entity.getId(), currentUserId);

            eventPublisher.publishEvent(new CollectionCreatedEvent(
                    entity.getId(),
                    currentUserId,
                    entity.getName()
            ));
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Failed to create collection: " + e.getMessage());
        }

        return collectionMapper.toResponse(entity);
    }

    public UserCollectionResponse getById(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getIsPublic() && !entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("Collection is private");
        }

        return collectionMapper.toResponse(entity);
    }

    public Page<UserCollectionResponse> search(String search, Pageable pageable) {
        if (search == null) search = "";
        return collectionRepository.findByIsPublicTrueAndNameContainingIgnoreCase(search, pageable)
                .map(collectionMapper::toResponse);
    }

    public Page<UserCollectionResponse> getMyCollections(String search, Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        if (search == null || search.isBlank()) {
            return collectionRepository.findByAuthorId(currentUserId, pageable)
                    .map(collectionMapper::toResponse);
        }
        return collectionRepository.findByAuthorIdAndNameContainingIgnoreCase(currentUserId, search, pageable)
                .map(collectionMapper::toResponse);
    }

    @Transactional
    public UserCollectionResponse update(UUID id, CollectionUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();

        var entity = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this collection");
        }

        // Check if new name already exists (and it's different from current)
        if (request.name() != null && !request.name().equals(entity.getName())) {
            if (collectionRepository.existsByAuthorIdAndNameAndIdNot(currentUserId, request.name(), id)) {
                throw new ResourceAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
            }
        }

        entity = collectionMapper.partialUpdate(request, entity);

        entity = collectionRepository.save(entity);

        eventPublisher.publishEvent(new CollectionUpdatedEvent(entity.getId(), currentUserId));

        log.debug("Updated collection: id={}", id);
        return collectionMapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this collection");
        }

        collectionRepository.delete(entity);

        eventPublisher.publishEvent(new CollectionDeletedEvent(entity.getId(), currentUserId));

        log.info("Deleted collection: id={} by user {}", id, currentUserId);
    }

    @Transactional
    public UserCollectionResponse generateShareToken(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to share this collection");
        }

        // Generate a unique token
        String token = generateUniqueToken();
        entity.setShareToken(token);
        entity = collectionRepository.save(entity);

        log.info("Generated share token for collection: id={} by user {}", id, currentUserId);
        return collectionMapper.toResponse(entity);
    }

    public UserCollectionResponse getByShareToken(String shareToken) {
        return collectionRepository.findByShareToken(shareToken)
                .map(collectionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found or link expired"));
    }

    @Transactional
    public UserCollectionResponse revokeShareToken(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to revoke share link");
        }

        entity.setShareToken(null);
        entity = collectionRepository.save(entity);

        log.info("Revoked share token for collection: id={} by user {}", id, currentUserId);
        return collectionMapper.toResponse(entity);
    }

    public UserCollectionResponse addTitles(UUID id, List<UUID> titleIds) {
        var currentUserId = getCurrentUserId();
        var existing = getById(id);
        List<UUID> combined = new ArrayList<>(existing.titleIds());

        // Find new titles being added
        List<UUID> newTitles = titleIds.stream()
                .filter(titleId -> !combined.contains(titleId))
                .toList();

        combined.addAll(titleIds);
        var update = new CollectionUpdateRequest(null, null, null, combined);
        var result = update(id, update);

        newTitles.forEach(titleId ->
                eventPublisher.publishEvent(new CollectionTitleAddedEvent(id, titleId, currentUserId))
        );

        return result;
    }

    public UserCollectionResponse removeTitle(UUID id, UUID titleId) {
        var currentUserId = getCurrentUserId();
        var existing = getById(id);
        var ids = existing.titleIds().stream().filter(t -> !t.equals(titleId)).toList();
        var update = new CollectionUpdateRequest(null, null, null, ids);
        var result = update(id, update);

        eventPublisher.publishEvent(new CollectionTitleRemovedEvent(id, titleId, currentUserId));

        return result;
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private String generateUniqueToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        String token;
        do {
            random.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (collectionRepository.existsByShareToken(token));
        return token;
    }

}
