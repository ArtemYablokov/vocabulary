package com.yablokovs.vocabulary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Definition {

    @Id
    // TODO: 20.10.2022 generators
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
//    @SequenceGenerator(name = "sequence_generator")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    Part part;

    // TODO: 28.10.2022 BUG phrases are not saving

    // если для одного слова изм фраза и удалено как раз второе слово, из которого идет ссылка?
    // тогда нужен явный функционал добавления фразы к слову - отдельной кнопкой при создании фразы :) -> ManyToMany
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    List<Phrase> phrases = new ArrayList<>();

    @Override
    public String toString() {
        return "Definition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", part=" + part +
                '}';
    }
}
