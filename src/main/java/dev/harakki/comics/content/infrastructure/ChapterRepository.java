package dev.harakki.comics.content.infrastructure;

import dev.harakki.comics.content.domain.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    // Fetch all chapters for a given title, ordered by number and subNumber
    @Query("SELECT c FROM Chapter c WHERE c.titleId = :titleId ORDER BY c.number ASC, c.subNumber ASC")
    List<Chapter> findAllByTitleId(UUID titleId);

    // Fetch chapter with its pages eagerly loaded
    @Query("SELECT c FROM Chapter c LEFT JOIN FETCH c.pages WHERE c.id = :id")
    Optional<Chapter> findByIdWithPages(UUID id);

}
