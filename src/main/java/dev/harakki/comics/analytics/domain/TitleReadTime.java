package dev.harakki.comics.analytics.domain;

import jakarta.persistence.*;
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
@Table(name = "title_read_times", indexes = {
        @Index(name = "idx_title_read_times_title_id", columnList = "titleId"),
        @Index(name = "idx_title_read_times_user_id", columnList = "userId")
})
@EntityListeners(AuditingEntityListener.class)
public class TitleReadTime {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID titleId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long totalReadTimeMillis;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

}
