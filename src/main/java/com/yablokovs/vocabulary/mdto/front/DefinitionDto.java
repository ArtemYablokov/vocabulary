package com.yablokovs.vocabulary.mdto.front;

import lombok.Data;

import java.util.List;

@Data
public class DefinitionDto {
    private String name;

    List<PhraseDto> phrases;

}
