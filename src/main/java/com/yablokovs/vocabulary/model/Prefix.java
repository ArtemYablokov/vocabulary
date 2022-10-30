package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
public class Prefix {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    // односторонняя связь - словам не нужно знать о префиксах) но не OneToMany тк у одного слова не один префикс
    @ManyToMany
    @JoinTable(name = "prefixes_words",
            joinColumns = @JoinColumn(name = "prefix_id"),
            inverseJoinColumns = @JoinColumn(name = "word_id"))
    public Set<Word> words;

    @Override
    public String toString() {
        return "Prefix{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
