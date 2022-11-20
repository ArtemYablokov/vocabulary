package com.yablokovs.vocabulary.mdto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<SynonymOrAntonymStringHolder> synonyms;
    private List<SynonymOrAntonymStringHolder> antonyms;

    public PartDto(String name) {
        this.name = name;
    }

    public PartDto(String name, List<SynonymOrAntonymStringHolder> synonyms) {
        this.name = name;
        this.synonyms = synonyms;
    }
}
