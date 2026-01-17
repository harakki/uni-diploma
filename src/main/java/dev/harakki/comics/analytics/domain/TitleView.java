package dev.harakki.comics.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "title_views", indexes = {
        @Index(name = "idx_title_views_title_id", columnList = "titleId"),
        @Index(name = "idx_title_views_user_id", columnList = "userId"),
        @Index(name = "idx_title_views_view_date", columnList = "viewDate")
})
@EntityListeners(AuditingEntityListener.class)
public class TitleView {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID titleId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private LocalDate viewDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

}
