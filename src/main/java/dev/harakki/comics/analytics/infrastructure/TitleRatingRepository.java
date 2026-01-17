package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleRatingRepository extends JpaRepository<TitleRating, UUID> {

    @Query("SELECT tr FROM TitleRating tr WHERE tr.titleId = :titleId AND tr.userId = :userId")
    Optional<TitleRating> findByTitleIdAndUserId(UUID titleId, UUID userId);

    @Query("SELECT AVG(tr.rating) FROM TitleRating tr WHERE tr.titleId = :titleId")
    Double getAverageRatingForTitle(UUID titleId);

}
