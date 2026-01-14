package dev.harakki.comics.media.application;

import dev.harakki.comics.media.domain.Media;
import dev.harakki.comics.media.domain.MediaStatus;
import dev.harakki.comics.media.infrastructure.MediaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MediaCleanupSchedulerTest {

    @Mock
    MediaRepository mediaRepository;

    @Mock
    S3Client s3Client;

    MediaCleanupScheduler scheduler;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        scheduler = new MediaCleanupScheduler(mediaRepository, s3Client);
        ReflectionTestUtils.setField(scheduler, "bucket", "test-bucket");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldDeleteOrphanFiles() {
        // Mock DB returning orphans
        var orphan = Media.builder().id(UUID.randomUUID()).s3Key("orphan.jpg").status(MediaStatus.PENDING).build();
        when(mediaRepository.findAllByStatusAndCreatedAtBefore(eq(MediaStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of(orphan));

        // Mock S3 success
        when(s3Client.deleteObjects(any(Consumer.class))).thenReturn(DeleteObjectsResponse.builder().deleted(d -> d.key("orphan.jpg")).build());

        // Execute
        scheduler.removeOrphanFiles();

        // Verify S3 called
        verify(s3Client).deleteObjects(any(Consumer.class));

        // Verify DB deletion called
        verify(mediaRepository).deleteAll(anyList());
    }

    @Test
    void shouldDoNothingIfNoOrphans() {
        when(mediaRepository.findAllByStatusAndCreatedAtBefore(any(), any())).thenReturn(List.of());

        scheduler.removeOrphanFiles();

        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        verify(mediaRepository, never()).deleteAll(any());
    }

}
