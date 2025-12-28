package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Title;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleRepository extends JpaRepository<Title, UUID>, JpaSpecificationExecutor<Title> {

    boolean existsByName(@NotBlank String name);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(@NotBlank String slug, UUID id);

    Optional<Title> findBySlug(String slug);

}
