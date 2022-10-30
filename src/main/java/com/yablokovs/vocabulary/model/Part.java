package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Part {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    Word word;

    // one directional - maybe from other side? надо проверить - должно быть интересно
    // если поиск по definition - тогда не Embedded !
    @OneToMany(mappedBy = "part", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    List<Definition> definitions;

    @ManyToMany
    @JoinTable(name = "part_synonym_antonym")
    List<Part> synonyms;

    @ManyToMany
    @JoinTable(name = "part_synonym_antonym")
    List<Part> antonyms;

    // Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn
    @ManyToMany
    @JoinTable(name = "rus_eng_synonym")
    List<WordRus> synonymsRus;

    @Override
    public String toString() {
        return "Part{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", word=" + word +
                '}';
    }
}
