package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class WordRus implements PartAndWordRus {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column(name = "name")
    private String name;

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    @ManyToMany(mappedBy = "synonymsRus")
    List<Part> synonymsEng = new ArrayList<>();

    @ManyToMany(mappedBy = "antonymsRus")
    List<Part> antonymsEng = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "rus_synonym",
            joinColumns = @JoinColumn(name = "word_rus_id"),
            inverseJoinColumns = @JoinColumn(name = "synonym_rus_id"))
    List<WordRus> synonyms = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "rus_antonym",
            joinColumns = @JoinColumn(name = "word_rus_id"),
            inverseJoinColumns = @JoinColumn(name = "antonym_rus_id"))
    List<WordRus> antonyms = new ArrayList<>();

    @Override
    public String toString() {
        return "WordRus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    // TODO: 4/9/23 was a bug - lomboks access methods wasn't seen by MVN
//    @Override
//    public Long getId() {
//        return id;
//    }
//
//    @Override
//    public List<WordRus> getSynonyms() {
//        return synonyms;
//    }
//
//    @Override
//    public List<WordRus> getAntonyms() {
//        return antonyms;
//    }
}
