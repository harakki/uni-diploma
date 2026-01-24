package dev.harakki.comics.collections.infrastructure;

import dev.harakki.comics.collections.domain.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    Page<Collection> findByIsPublicTrueAndNameContainingIgnoreCase(String search, Pageable pageable);

    Page<Collection> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Collection> findByAuthorIdAndNameContainingIgnoreCase(UUID authorId, String name, Pageable pageable);

    boolean existsByAuthorIdAndName(UUID authorId, String name);

    boolean existsByAuthorIdAndNameAndIdNot(UUID authorId, String name, UUID id);

    Optional<Collection> findByShareToken(String shareToken);

    boolean existsByShareToken(String shareToken);

}
