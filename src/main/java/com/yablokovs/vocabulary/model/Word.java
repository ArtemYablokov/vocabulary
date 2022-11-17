package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
// TODO: 01.10.2022 check @DATA vs HIBERNATE
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "word")
public class Word {
    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column
    private Long numberOfSearches;

    // TODO: 26.10.2022 should not create separate table ! mappedBy?“
    //  The mappedBy attribute characterizes a bidirectional association and must be set on the parent-side”
    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    Set<Part> parts = new HashSet<>();

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    Timestamp updatedAt;

    //    @Embedded 'Embedded' attribute type should not be a container
    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    List<WhenSearched> searchedAt;

    @ManyToMany(mappedBy = "words")
    List<Tag> tags;

    // redundant - not adding PREFIX to word anywhere. Don't need to get PREFIXES from WORD
    @ManyToMany(mappedBy = "words")
    Set<Prefix> prefixes;

    public Word(String name) {
        this.name = name;
    }

    public void addPart(Part part) {
        part.setWord(this);
        parts.add(part);
    }


    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numberOfSearches=" + numberOfSearches +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
