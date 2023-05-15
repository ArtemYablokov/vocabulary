package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class WhenSearched {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "whensearched_id_seq")
    @SequenceGenerator(name = "whensearched_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    Word word;

    @Column
    LocalDateTime whenSearched;

    @Override
    public String toString() {
        return "WhenSearched{" +
                "id=" + id +
                ", word=" + word +
                ", createdAt=" + whenSearched +
                '}';
    }
}
