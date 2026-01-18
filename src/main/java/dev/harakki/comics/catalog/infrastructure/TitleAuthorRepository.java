package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.TitleAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TitleAuthorRepository extends JpaRepository<TitleAuthor, UUID> {

    @Query("SELECT COALESCE(MAX(ta.sortOrder), -1) FROM TitleAuthor ta WHERE ta.title.id = :titleId")
    int findMaxSortOrderByTitleId(UUID titleId);

}
