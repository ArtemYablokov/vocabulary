package com.yablokovs.vocabulary.model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Prefix {

    // точно односторонняя связь - словам не нужно знать о префиксах)))
    @ManyToMany // не OneToMany тк у одного слова не один префикс
    List<Word> words;
}
