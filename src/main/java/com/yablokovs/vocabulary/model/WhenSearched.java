package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class WhenSearched {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    Word word;

    @Column
    Timestamp createdAt;

    @Override
    public String toString() {
        return "WhenSearched{" +
                "id=" + id +
                ", word=" + word +
                ", createdAt=" + createdAt +
                '}';
    }
}
