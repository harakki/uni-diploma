package dev.harakki.comics.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_interactions", indexes = {
        @Index(name = "idx_interactions_user_target", columnList = "userId, targetId"), // "Did I read that?"
        @Index(name = "idx_interactions_type", columnList = "type") // "Show me all likes"
})
@EntityListeners(AuditingEntityListener.class)
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    // ID of the target entity (e.g., chapterId, titleId)
    @Column(nullable = false)
    private UUID targetId;

    // Ex.: {"readTimeMs": 50000, "rating": 9}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

}
