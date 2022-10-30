package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Definition {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    Part part;

    // TODO: 28.10.2022 BUG phrases are not saving
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)// если для одного слова изм фраза и удалено как раз второе слово, из которого идет ссылка?
            // тогда нужен явный функционал добавления фразы к слову - отдельной кнопкой при создании фразы :) -> ManyToMany
            List<Phrase> phrases;

    @Override
    public String toString() {
        return "Definition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", part=" + part +
                '}';
    }
}
