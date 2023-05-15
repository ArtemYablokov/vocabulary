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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "definition_id_seq")
    @SequenceGenerator(name = "definition_id_seq", allocationSize = 1)
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
