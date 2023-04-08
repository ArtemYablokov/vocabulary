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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE)
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
