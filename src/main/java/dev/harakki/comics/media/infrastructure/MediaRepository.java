package dev.harakki.comics.media.infrastructure;

import dev.harakki.comics.media.domain.Media;
import dev.harakki.comics.media.domain.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    List<Media> findAllByStatusAndCreatedAtBefore(MediaStatus status, Instant createdAtBefore);

}
