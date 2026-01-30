package dev.harakki.comics.content.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
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
@Table(name = "chapters",
        indexes = {
                @Index(name = "idx_chapter_title_id", columnList = "titleId"),
                @Index(name = "idx_chapter_number", columnList = "number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_chapter_title_number_subnumber", columnNames = {"titleId", "number", "subNumber"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Chapter {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private UUID titleId;

    private Integer volume;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer subNumber;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("pageOrder ASC")
    private List<Page> pages = new ArrayList<>();

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

    public String getDisplayNumber() {
        return (subNumber == 0) ? String.valueOf(number) : number + "." + subNumber;
    }

}
