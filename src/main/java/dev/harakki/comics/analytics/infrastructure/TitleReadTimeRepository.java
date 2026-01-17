package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleReadTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleReadTimeRepository extends JpaRepository<TitleReadTime, UUID> {

    @Query("SELECT trt FROM TitleReadTime trt WHERE trt.titleId = :titleId AND trt.userId = :userId")
    Optional<TitleReadTime> findByTitleIdAndUserId(UUID titleId, UUID userId);

}
