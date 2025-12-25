package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    boolean existsByName(@NotBlank String name);

    boolean existsBySlug(@NotBlank String slug);

    Optional<Tag> findBySlug(String slug);

}
