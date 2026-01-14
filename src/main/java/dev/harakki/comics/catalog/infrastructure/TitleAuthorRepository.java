package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.TitleAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TitleAuthorRepository extends JpaRepository<TitleAuthor, UUID> {

    int findMaxSortOrderByTitleId(UUID titleId);

}
