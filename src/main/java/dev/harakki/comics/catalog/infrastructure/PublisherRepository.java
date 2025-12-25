package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Publisher;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, UUID> {

    boolean existsByName(@NotBlank String name);

    boolean existsBySlug(String slug);

    Optional<Publisher> findBySlug(String slug);
    
}
