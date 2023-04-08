package com.yablokovs.vocabulary.mdto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<StringHolder> synonyms;
    private List<StringHolder> antonyms;
    private List<StringHolder> rusSynonyms;
    private List<StringHolder> rusAntonyms;

    public PartDto(String name) {
        this.name = name;
    }

    public PartDto(String name, List<StringHolder> synonyms) {
        this.name = name;
        this.synonyms = synonyms;
    }
}
