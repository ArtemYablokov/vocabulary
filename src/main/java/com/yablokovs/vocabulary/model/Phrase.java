package com.yablokovs.vocabulary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Phrase {

    @Id
    // TODO: 20.10.2022 generators
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
//    @SequenceGenerator(name = "sequence_generator")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "name")
    private String name;

//    @ManyToMany(mappedBy = "phrases") // фразы точно должны быть связаны с частями речи! +++
    // Illegal use of mappedBy on both sides of the relationship: com.yablokovs.vocabulary.model.PartOfSpeech.phrases

    @JsonIgnore
    @ManyToMany(mappedBy = "phrases")
    List<Definition> definitions = new ArrayList<>();


    @Override
    public String toString() {
        return "Phrase{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

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
