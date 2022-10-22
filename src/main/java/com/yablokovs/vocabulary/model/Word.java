package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
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

    @OneToMany
    List<PartOfSpeech> partOfSpeeches;

    @Column
    Timestamp createdAt;

    @Column
    Timestamp updatedAt;

    // не Embedded тк будет поиск по периоду поиска
//    @Embedded 'Embedded' attribute type should not be a container
    @OneToMany
    List<Date> searchedAt;

}
