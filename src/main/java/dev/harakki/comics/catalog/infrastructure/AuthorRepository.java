package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Author;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {
    
    boolean existsByName(@NotBlank String name);

    boolean existsBySlug(String slug);

    Optional<Author> findBySlug(String slug);
    
}
