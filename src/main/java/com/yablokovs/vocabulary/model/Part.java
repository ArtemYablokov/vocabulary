package com.yablokovs.vocabulary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Part {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "name")
    private String name;

    // TODO: 15.11.2022 need a cheap way to get name from WORD
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    Word word;

    // one directional - maybe from other side? надо проверить - должно быть интересно
    // если поиск по definition - тогда не Embedded !
    @OneToMany(mappedBy = "part", cascade = CascadeType.PERSIST, orphanRemoval = true)
    List<Definition> definitions = new ArrayList<>();

    // TODO: BUG 20.11.2022 need to fetch
    @ManyToMany
    @JoinTable(name = "part_synonym",
            joinColumns = @JoinColumn(name = "synonym_id"),
            inverseJoinColumns = @JoinColumn(name = "part_id"))
    List<Part> synonyms = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "part_antonym",
            joinColumns = @JoinColumn(name = "antonym_id"),
            inverseJoinColumns = @JoinColumn(name = "part_id"))
    List<Part> antonyms = new ArrayList<>();

    // Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn
    @ManyToMany
    @JoinTable(name = "rus_eng_synonym")
    List<WordRus> synonymsRus = new ArrayList<>();


    public Part(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Part{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", word=" + word +
                '}';
    }
}
