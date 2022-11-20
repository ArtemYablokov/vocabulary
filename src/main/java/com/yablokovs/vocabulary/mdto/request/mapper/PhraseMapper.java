package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.PhraseDto;
import com.yablokovs.vocabulary.model.Phrase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class PhraseMapper {

    public abstract Phrase toPhrase(PhraseDto phraseDto);

    public abstract PhraseDto toPhraseDto(Phrase phrase);
}
