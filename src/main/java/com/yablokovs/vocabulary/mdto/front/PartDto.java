package com.yablokovs.vocabulary.mdto.front;

import lombok.Data;

import java.util.List;

@Data
public class PartDto {
    private String name;
    private List<DefinitionDto> definitions;
    private List<String> synonyms;
    private List<String> antonyms;
}
