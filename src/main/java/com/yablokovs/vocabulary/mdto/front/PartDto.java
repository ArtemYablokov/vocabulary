package com.yablokovs.vocabulary.mdto.front;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<String> synonyms;
    private List<String> antonyms;
}
