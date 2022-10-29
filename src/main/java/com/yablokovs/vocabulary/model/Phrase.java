package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Data
public class Phrase {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

//    @ManyToMany(mappedBy = "phrases") // фразы точно должны быть связаны с частями речи! +++
    // Illegal use of mappedBy on both sides of the relationship: com.yablokovs.vocabulary.model.PartOfSpeech.phrases

    @ManyToMany(mappedBy = "phrases")
    List<Definition> definitions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phrase phrase = (Phrase) o;
        return Objects.equals(name, phrase.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
