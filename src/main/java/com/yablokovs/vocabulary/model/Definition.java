package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
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

    @ManyToMany(mappedBy = "definitions")// если для одного слова изм фраза и удалено как раз второе слово, из которого идет ссылка?
    // тогда нужен явный функционал добавления фразы к слову - отдельной кнопкой при создании фразы :) -> ManyToMany
    List<Phrase> phrases;
}
