package dev.harakki.comics.media.application;

import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaFixateRequestedEvent;
import dev.harakki.comics.media.infrastructure.MediaRepository;
import dev.harakki.comics.shared.exception.ResourceNotUploadedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaEventListener {

    private final MediaRepository mediaRepository;

    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    @Async
    @Transactional
    @ApplicationModuleListener
    @Retryable(
            retryFor = {NoSuchKeyException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000)
    )
    public void on(MediaFixateRequestedEvent event) {
        log.debug("Attempting to fixate media: {}", event.mediaId());

        var media = mediaRepository.findById(event.mediaId())
                .orElse(null);
        if (media == null) {
            return;
        }

        var headObject = s3Client.headObject(b -> b.bucket(bucket).key(media.getS3Key()));
        media.setSize(headObject.contentLength());
        media.setContentType(headObject.contentType());
        media.commit();

        mediaRepository.save(media);
        log.info("Media {} fixated successfully.", media.getId());
    }

    @Async
    @Transactional
    @ApplicationModuleListener
    public void on(MediaDeleteRequestedEvent event) {
        mediaRepository.findById(event.mediaId()).ifPresent(media -> {
            try {
                deleteFromS3(media.getS3Key());
                mediaRepository.delete(media);
            } catch (Exception e) {
                log.error("Failed to delete media {} from S3. DB record kept for retry/manual cleanup.", media.getId(), e);
            }
        });
    }

    private void deleteFromS3(String key) {
        s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    }

}
