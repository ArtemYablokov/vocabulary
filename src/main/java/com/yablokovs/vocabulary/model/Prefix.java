package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Prefix {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "name")
    private String name;

    // односторонняя связь - словам не нужно знать о префиксах) но не OneToMany тк у одного слова не один префикс
    /**
     * NEVER use CASCADE TYPE PERSIST!!! prefix is saving after word already saved!
     * */
    @ManyToMany
    @JoinTable(name = "prefixes_words",
            joinColumns = @JoinColumn(name = "prefix_id"),
            inverseJoinColumns = @JoinColumn(name = "word_id"))
    private Set<Word> words = new HashSet<>();

    @Override
    public String toString() {
        return "Prefix{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
