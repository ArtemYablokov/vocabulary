package com.yablokovs.vocabulary.mdto.externalApi.localMapper;

import java.util.List;

public class Meaning {
    private final List<String> synonyms;
    private final List<String> antonyms;
    private final List<DefinitionToExampleTuple> definitionToExampleTuples;


    public Meaning(List<String> synonyms, List<String> antonyms, List<DefinitionToExampleTuple> definitionToExampleTuples) {
        this.synonyms = synonyms;
        this.antonyms = antonyms;
        this.definitionToExampleTuples = definitionToExampleTuples;
    }
}
