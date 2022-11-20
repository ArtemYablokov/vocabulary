package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.WordRequest;
import com.yablokovs.vocabulary.model.Word;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PartMapper.class})
public abstract class WordMapper {

    public abstract Word toWord(WordRequest wordRequest);


    public abstract WordRequest toWordRequest(Word word);
}
