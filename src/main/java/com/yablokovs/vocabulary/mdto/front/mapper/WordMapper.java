package com.yablokovs.vocabulary.mdto.front.mapper;

import com.yablokovs.vocabulary.mdto.front.WordDto;
import com.yablokovs.vocabulary.model.Word;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PartMapper.class})
public abstract class WordMapper {

    public abstract Word toWord(WordDto wordDto);
}
