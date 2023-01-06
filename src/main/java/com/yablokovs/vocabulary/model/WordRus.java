package com.yablokovs.vocabulary.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class WordRus {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "synonymsRus")
    List<Part> synonymsEng = new ArrayList<>();

    @Override
    public String toString() {
        return "WordRus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
