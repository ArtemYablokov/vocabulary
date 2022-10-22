package com.yablokovs.vocabulary.mdto.externalApi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class Meaning{
    private String partOfSpeech;
    private List<Definition> definitions;
    private List<String> synonyms;
    private List<String> antonyms;
}
