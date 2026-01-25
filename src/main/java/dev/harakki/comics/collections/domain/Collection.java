package dev.harakki.comics.collections.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "user_collections", indexes = {
        @Index(name = "idx_collection_author", columnList = "authorId"),
        @Index(name = "idx_collection_author_name", columnList = "authorId, name", unique = true),
        @Index(name = "idx_collection_share_token", columnList = "shareToken", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Collection {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublic = false;

    @Column(unique = true)
    private String shareToken;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "collection_titles", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "title_id")
    @OrderColumn(name = "sort_order")
    private List<UUID> titleIds = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private Instant updatedAt;

    @Version
    private Long version;

}
