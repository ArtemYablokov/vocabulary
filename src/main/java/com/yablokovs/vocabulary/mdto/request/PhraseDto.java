package com.yablokovs.vocabulary.mdto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhraseDto {
    private String name;

    public PhraseDto(String name) {
        this.name = name;
    }
}
