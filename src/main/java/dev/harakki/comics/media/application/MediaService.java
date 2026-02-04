package dev.harakki.comics.media.application;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaUrlProvider;
import dev.harakki.comics.media.domain.Media;
import dev.harakki.comics.media.domain.MediaStatus;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import dev.harakki.comics.media.infrastructure.MediaRepository;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService implements MediaUrlProvider {

    private static final long MEDIA_URL_EXPIRATION_MINUTES = 2 * 60L;
    private static final long UPLOAD_MEDIA_URL_EXPIRATION_MINUTES = 15L;

    private final MediaRepository mediaRepository;

    private final S3Presigner s3Presigner;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${s3.bucket}")
    private String bucket;

    @Transactional
    public MediaUploadUrlResponse getUploadUrl(String originalFilename, String contentType, Integer width,
                                               Integer height) {
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only images are allowed for chapter pages");
        }

        UUID mediaId = UuidCreator.getTimeOrderedEpoch();
        // Path generation: uploads/{id}/filename.ext
        String s3Key = "uploads/" + mediaId + "/" + originalFilename;

        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(UPLOAD_MEDIA_URL_EXPIRATION_MINUTES))
                .putObjectRequest(request -> request
                        .bucket(bucket)
                        .key(s3Key)
                        .contentType(contentType)
                        .build())
                .build();

        // Save "promise" of the file (file in PENDING status)
        mediaRepository.save(Media.builder()
                .id(mediaId)
                .bucket(bucket)
                .s3Key(s3Key)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .width(width)
                .height(height)
                .status(MediaStatus.PENDING)
                .isNew(true)
                .build());

        return new MediaUploadUrlResponse(mediaId, s3Presigner.presignPutObject(presignRequest).url().toString(),
                s3Key);
    }

    @Transactional(readOnly = true)
    public String getPublicUrl(UUID mediaId) {
        return mediaRepository.findById(mediaId)
                .map(media -> {
                    // Generate presigned GET URL
                    var request = GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(MEDIA_URL_EXPIRATION_MINUTES))
                            .getObjectRequest(b -> b.bucket(bucket).key(media.getS3Key()))
                            .build();
                    return s3Presigner.presignGetObject(request).url().toString();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Media with id " + mediaId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public String getPublicUrl(String s3Key) {
        var request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(MEDIA_URL_EXPIRATION_MINUTES))
                .getObjectRequest(b -> b.bucket(bucket).key(s3Key))
                .build();
        return s3Presigner.presignGetObject(request).url().toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getPublicUrls(List<String> s3Keys) {
        return s3Keys.stream().collect(Collectors.toMap(key -> key, this::getPublicUrl));
    }

    @Transactional
    public void deleteMediaById(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media with id " + mediaId + " not found"));

        eventPublisher.publishEvent(new MediaDeleteRequestedEvent(mediaId));
        log.info("Deleted media: id={}", mediaId);
    }

}
