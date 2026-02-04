package dev.harakki.comics.media.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "media")
@EntityListeners(AuditingEntityListener.class)
public class Media implements Persistable<UUID> {

    @Id
    @Column(updatable = false)
    private UUID id;

    private String bucket;

    @Column(nullable = false)
    private String s3Key;

    private String originalFilename;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MediaStatus status = MediaStatus.PENDING;

    private String contentType;

    private Long size;

    private Integer width;

    private Integer height;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;

    @Version
    private Long version;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    public void commit() {
        this.status = MediaStatus.COMMITTED;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad // After loading from DB
    @PostPersist
        // After saving to DB
    void markNotNew() {
        this.isNew = false;
    }

}
