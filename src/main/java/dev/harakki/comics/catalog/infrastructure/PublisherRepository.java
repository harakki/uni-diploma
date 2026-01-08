package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Publisher;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, UUID>, JpaSpecificationExecutor<Publisher> {

    boolean existsByName(@NotBlank String name);

    boolean existsByNameAndIdNot(@NotBlank String name, UUID id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(@NotBlank String slug, UUID id);

    Optional<Publisher> findBySlug(String slug);

}
