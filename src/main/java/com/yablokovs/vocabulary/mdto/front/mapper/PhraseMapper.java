package com.yablokovs.vocabulary.mdto.front.mapper;

import com.yablokovs.vocabulary.mdto.front.PhraseDto;
import com.yablokovs.vocabulary.model.Phrase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class PhraseMapper {

    public abstract Phrase toPhrase(PhraseDto phraseDto);
}
