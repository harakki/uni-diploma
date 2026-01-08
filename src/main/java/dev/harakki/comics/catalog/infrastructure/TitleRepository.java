package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Title;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleRepository extends JpaRepository<Title, UUID>, JpaSpecificationExecutor<Title> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"authors", "authors.author", "tags", "publisher"})
    Page<Title> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"authors", "authors.author", "tags", "publisher"})
    Page<Title> findAll(@Nullable Specification<Title> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"authors", "authors.author", "tags", "publisher"})
    Optional<Title> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"authors", "authors.author", "tags", "publisher"})
    Optional<Title> findBySlug(String slug);

    boolean existsByName(@NotBlank String name);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(@NotBlank String slug, UUID id);

}
