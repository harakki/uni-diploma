package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.SlugSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SlugSequenceRepository extends JpaRepository<SlugSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SlugSequence s WHERE s.slugPrefix = :slugPrefix")
    Optional<SlugSequence> findBySlugPrefixWithLock(String slugPrefix);

}
