package com.yablokovs.vocabulary.mdto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DefinitionDto {
    private String name;

    List<PhraseDto> phrases;

    public DefinitionDto(String name) {
        this.name = name;
    }
}
