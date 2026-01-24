package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.domain.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    boolean existsByUserIdAndTargetIdAndType(UUID userId, UUID targetId, InteractionType type);

    @Query("SELECT ui.targetId FROM UserInteraction ui WHERE ui.userId = :userId AND ui.targetId IN :chapterIds AND ui.type = :type")
    List<UUID> findReadChapterIds(UUID userId, List<UUID> chapterIds, InteractionType type);

}
