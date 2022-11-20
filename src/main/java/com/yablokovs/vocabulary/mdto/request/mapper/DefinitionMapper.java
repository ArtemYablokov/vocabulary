package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.DefinitionDto;
import com.yablokovs.vocabulary.model.Definition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PhraseMapper.class})
public abstract class DefinitionMapper {

    public abstract Definition toDefinition(DefinitionDto definitionDto);

    public abstract DefinitionDto toDefinitionDto(Definition definition);
}
