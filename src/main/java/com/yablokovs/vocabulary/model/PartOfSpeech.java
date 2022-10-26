package com.yablokovs.vocabulary.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class PartOfSpeech {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    Word word;

    // one directional - maybe from other side? надо проверить - должно быть интересно
    // если поиск по definition - тогда не Embedded !
    @OneToMany
    List<Definition> definitions;

    @ManyToMany
    List<Word> synonyms;

    @ManyToMany
    List<WordRus> synonymsRus;

    @ManyToMany // если для одного слова изм фраза и удалено как раз второе слово, из которого идет ссылка?
            // тогда нужен явный функционал добавления фразы к слову - отдельной кнопкой при создании фразы :) -> ManyToMany
    List<Phrase> phrases;

    @ManyToMany
    List<Tags> tags;
}
