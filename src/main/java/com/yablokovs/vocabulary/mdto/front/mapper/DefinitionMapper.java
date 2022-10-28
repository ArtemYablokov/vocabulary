package com.yablokovs.vocabulary.mdto.front.mapper;

import com.yablokovs.vocabulary.mdto.front.DefinitionDto;
import com.yablokovs.vocabulary.model.Definition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PhraseMapper.class})
public abstract class DefinitionMapper {

    public abstract Definition toDefinition(DefinitionDto definitionDto);
}
