package dev.harakki.comics.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "slug_sequences")
public class SlugSequence {

    @Id
    @Column(nullable = false)
    private String slugPrefix;

    @Column(nullable = false)
    private Long counter;

}
