package dev.harakki.comics.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library_entries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_library_user_title", columnNames = {"userId", "titleId"})
        },
        indexes = {
                @Index(name = "idx_library_user_status", columnList = "userId, status"),
                @Index(name = "idx_library_user", columnList = "userId"),
                @Index(name = "idx_library_title", columnList = "titleId")
        }
)

@EntityListeners(AuditingEntityListener.class)
public class LibraryEntry {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID titleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    @Min(1)
    @Max(10)
    private Integer rating;

    // Reading progress
    private UUID lastReadChapterId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private Instant updatedAt;

    @Version
    private Long version;

}
