package dev.harakki.comics.catalog.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.Year;
import java.util.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "titles", indexes = {
        @Index(name = "idx_title_created_at", columnList = "createdAt"),
        @Index(name = "idx_title_release_year", columnList = "releaseYear"),
        @Index(name = "idx_title_status", columnList = "titleStatus"),
        @Index(name = "idx_title_publisher", columnList = "publisher_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Title {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TitleType type;

    @Enumerated(EnumType.STRING)
    private TitleStatus titleStatus;

    @PastOrPresent
    private Year releaseYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentRating contentRating;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isLicensed = false;

    @Pattern(regexp = "^[A-Z]{2}$")
    @Column(columnDefinition = "CHAR(2)")
    private String countryIsoCode;

    private UUID mainCoverMediaId;

    @Builder.Default
    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TitleAuthor> authors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "title_tags",
            joinColumns = @JoinColumn(name = "title_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private UUID updatedBy;

    @Version
    private Long version;

}
