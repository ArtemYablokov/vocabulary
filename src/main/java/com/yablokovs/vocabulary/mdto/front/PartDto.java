package com.yablokovs.vocabulary.mdto.front;

import com.yablokovs.vocabulary.model.Part;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<SynonymAntonymHolder> synonyms;
    private List<SynonymAntonymHolder> antonyms;

    public PartDto(String name) {
        this.name = name;
    }

    public PartDto(String name, List<SynonymAntonymHolder> synonyms) {
        this.name = name;
        this.synonyms = synonyms;
    }
}
