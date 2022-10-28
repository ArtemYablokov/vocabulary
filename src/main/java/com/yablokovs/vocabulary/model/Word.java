package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
// TODO: 01.10.2022 check @DATA vs HIBERNATE
@Data
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "word", orphanRemoval = true)
    List<Part> parts;

    @Column
    Timestamp createdAt;

    @Column
    Timestamp updatedAt;

    // не Embedded тк будет поиск по периоду поиска
//    @Embedded 'Embedded' attribute type should not be a container
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "word", orphanRemoval = true)
    List<WhenSearched> searchedAt;

    @ManyToMany(mappedBy = "words")
    List<Tags> tags;
}
