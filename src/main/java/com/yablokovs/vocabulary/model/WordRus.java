package com.yablokovs.vocabulary.model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class WordRus {


    @ManyToMany
    List<Word> synonymsEng;
}