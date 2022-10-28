package com.yablokovs.vocabulary.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class WordRus {

    @ManyToMany(mappedBy = "synonymsRus")
    List<Part> synonymsEng;
    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;
    @Column(name = "name")
    private String name;
}
