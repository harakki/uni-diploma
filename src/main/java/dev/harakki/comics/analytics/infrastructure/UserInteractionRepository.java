package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.domain.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    long countByTargetIdAndType(UUID targetId, InteractionType type);

    @Query(value = """
                SELECT AVG(CAST(metadata ->> 'rating' AS INTEGER))
                FROM user_interactions
                WHERE target_id = :titleId AND type = 'TITLE_RATED'
            """, nativeQuery = true)
    Double getAverageRating(UUID titleId);

}
