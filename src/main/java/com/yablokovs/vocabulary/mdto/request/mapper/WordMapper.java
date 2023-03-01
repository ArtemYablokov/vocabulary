package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Word;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PartMapper.class})
public abstract class WordMapper {

    public abstract Word mapRequestToWordIgnoreSynonymsAndAntonyms(WordFrontEnd wordFrontEnd);


    public abstract WordFrontEnd toWordResponse(Word word);
}
