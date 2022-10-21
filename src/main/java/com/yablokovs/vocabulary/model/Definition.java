package com.yablokovs.vocabulary.model;

import javax.persistence.Entity;
import javax.persistence.Enumerated;

@Entity
public class Definition {


    @Enumerated()
    private PartOfSpeech partOfSpeech;
}
