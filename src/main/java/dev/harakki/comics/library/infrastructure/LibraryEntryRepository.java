package dev.harakki.comics.library.infrastructure;

import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.domain.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, UUID>, JpaSpecificationExecutor<LibraryEntry> {

    Optional<LibraryEntry> findByUserIdAndTitleId(UUID userId, UUID titleId);

    boolean existsByUserIdAndTitleId(UUID userId, UUID titleId);

    Page<LibraryEntry> findByUserId(UUID userId, Pageable pageable);

    Page<LibraryEntry> findByUserIdAndStatus(UUID userId, ReadingStatus status, Pageable pageable);

}
