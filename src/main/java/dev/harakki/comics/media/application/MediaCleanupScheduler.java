package dev.harakki.comics.media.application;

import dev.harakki.comics.media.domain.MediaStatus;
import dev.harakki.comics.media.domain.Media;
import dev.harakki.comics.media.infrastructure.MediaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class MediaCleanupScheduler {

    private final MediaRepository mediaRepository;

    private final S3Client s3Client;

    private static final long ORPHAN_THRESHOLD_MINUTES = 60L;

    @Value("${s3.bucket}")
    private String bucket;

    @Transactional
    @Scheduled(fixedRateString = "PT60M") // Run every 60 minutes
    public void removeOrphanFiles() {
        var timeThreshold = Instant.now().minus(ORPHAN_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        List<Media> orphans = mediaRepository.findAllByStatusAndCreatedAtBefore(MediaStatus.PENDING, timeThreshold);
        if (orphans.isEmpty()) {
            return;
        }
        log.info("Found {} orphan media files to delete", orphans.size());

        var objectIds = orphans.stream()
                .map(media -> ObjectIdentifier.builder().key(media.getS3Key()).build())
                .toList();

        try {
            // Bulk delete from S3
            var deleteResponse = s3Client.deleteObjects(b -> b
                    .bucket(bucket)
                    .delete(d -> d.objects(objectIds))
            );
            log.info("Deleted {} objects from S3", deleteResponse.deleted().size());
            if (deleteResponse.hasErrors()) {
                log.error("S3 Bulk delete had errors: {}", deleteResponse.errors());
            }

            // Bulk delete from DB
            mediaRepository.deleteAll(orphans);
        } catch (Exception e) {
            log.error("Failed to execute bulk cleanup", e);
        }
    }

}
