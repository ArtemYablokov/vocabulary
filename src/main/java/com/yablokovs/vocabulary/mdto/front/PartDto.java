package com.yablokovs.vocabulary.mdto.front;

import com.yablokovs.vocabulary.model.Part;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<Part> synonyms;
    private List<Part> antonyms;
}
