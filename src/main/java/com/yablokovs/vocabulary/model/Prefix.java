package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Prefix {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    // точно односторонняя связь - словам не нужно знать о префиксах)))
    @ManyToMany // не OneToMany тк у одного слова не один префикс
    List<Word> words;
}
