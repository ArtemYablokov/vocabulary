package com.yablokovs.vocabulary.model;


import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Phrase {

    @Id
    // TODO: 20.10.2022 generators
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany // фразы точно должны быть связаны с частями речи! +++
    /*(mappedBy = "phrases")*/      // Illegal use of mappedBy on both sides of the relationship: com.yablokovs.vocabulary.model.PartOfSpeech.phrases
    List<Definition> definitions;
}
