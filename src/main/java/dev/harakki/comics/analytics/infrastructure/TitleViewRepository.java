package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleViewRepository extends JpaRepository<TitleView, UUID> {

    @Query("SELECT tv FROM TitleView tv WHERE tv.titleId = :titleId AND tv.userId = :userId AND tv.viewDate = :viewDate")
    Optional<TitleView> findByTitleIdAndUserIdAndViewDate(UUID titleId, UUID userId, LocalDate viewDate);

    @Query("SELECT SUM(tv.viewCount) FROM TitleView tv WHERE tv.titleId = :titleId")
    Long getTotalViewCountForTitle(UUID titleId);

}
