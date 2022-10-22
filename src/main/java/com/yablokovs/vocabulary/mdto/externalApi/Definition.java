package com.yablokovs.vocabulary.mdto.externalApi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
public class Definition{
    @Getter
    private String definition;
    private List<Object> synonyms;
    private List<Object> antonyms;
    @Getter
    private String example;
}
