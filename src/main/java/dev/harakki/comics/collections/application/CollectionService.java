package dev.harakki.comics.collections.application;

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

    private final CollectionRepository repository;
    private final CollectionMapper mapper;

    @Transactional
    public UserCollectionResponse create(CollectionCreateRequest request) {
        UUID currentUserId = getCurrentUserId();

        if (repository.existsByAuthorIdAndName(currentUserId, request.name())) {
            throw new ResourceAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
        }

        var entity = mapper.toEntity(request);
        entity.setAuthorId(currentUserId);

        try {
            entity = repository.save(entity);
            repository.flush();
            log.info("Created collection {} by user {}", entity.getId(), currentUserId);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Failed to create collection: " + e.getMessage());
        }

        return mapper.toResponse(entity);
    }

    public UserCollectionResponse getById(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getIsPublic() && !entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("Collection is private");
        }

        return mapper.toResponse(entity);
    }

    public Page<UserCollectionResponse> search(String search, Pageable pageable) {
        if (search == null) search = "";
        return repository.findByIsPublicTrueAndNameContainingIgnoreCase(search, pageable)
                .map(mapper::toResponse);
    }

    public Page<UserCollectionResponse> getMyCollections(String search, Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        if (search == null || search.isBlank()) {
            return repository.findByAuthorId(currentUserId, pageable)
                    .map(mapper::toResponse);
        }
        return repository.findByAuthorIdAndNameContainingIgnoreCase(currentUserId, search, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public UserCollectionResponse update(UUID id, CollectionUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to update this collection");
        }

        // Check if new name already exists (and it's different from current)
        if (request.name() != null && !request.name().equals(entity.getName())) {
            if (repository.existsByAuthorIdAndNameAndIdNot(currentUserId, request.name(), id)) {
                throw new ResourceAlreadyExistsException("Collection with name '" + request.name() + "' already exists");
            }
        }

        entity = mapper.partialUpdate(request, entity);

        entity = repository.save(entity);

        log.debug("Updated collection: id={}", id);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to delete this collection");
        }

        repository.delete(entity);
        log.info("Deleted collection: id={} by user {}", id, currentUserId);
    }

    @Transactional
    public UserCollectionResponse generateShareToken(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to share this collection");
        }

        // Generate a unique token
        String token = generateUniqueToken();
        entity.setShareToken(token);
        entity = repository.save(entity);

        log.info("Generated share token for collection: id={} by user {}", id, currentUserId);
        return mapper.toResponse(entity);
    }

    public UserCollectionResponse getByShareToken(String shareToken) {
        return repository.findByShareToken(shareToken)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found or link expired"));
    }

    @Transactional
    public UserCollectionResponse revokeShareToken(UUID id) {
        UUID currentUserId = getCurrentUserId();

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        if (!entity.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to revoke share link");
        }

        entity.setShareToken(null);
        entity = repository.save(entity);

        log.info("Revoked share token for collection: id={} by user {}", id, currentUserId);
        return mapper.toResponse(entity);
    }

    public UserCollectionResponse addTitles(UUID id, List<UUID> titleIds) {
        var existing = getById(id);
        List<UUID> combined = new ArrayList<>(existing.titleIds());
        combined.addAll(titleIds);
        var update = new CollectionUpdateRequest(null, null, null, combined);
        return update(id, update);
    }

    public UserCollectionResponse removeTitle(UUID id, UUID titleId) {
        var existing = getById(id);
        var ids = existing.titleIds().stream().filter(t -> !t.equals(titleId)).toList();
        var update = new CollectionUpdateRequest(null, null, null, ids);
        return update(id, update);
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(SecurityUtils.getCurrentUserId());
    }

    private String generateUniqueToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        String token;
        do {
            random.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (repository.existsByShareToken(token));
        return token;
    }

}
