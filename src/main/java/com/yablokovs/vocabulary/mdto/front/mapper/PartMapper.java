package com.yablokovs.vocabulary.mdto.front.mapper;

import com.yablokovs.vocabulary.mdto.front.PartDto;
import com.yablokovs.vocabulary.model.Part;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DefinitionMapper.class, PartMapper.class})
public abstract class PartMapper {

    @Mapping(ignore = true, target = "synonyms")
    @Mapping(ignore = true, target = "antonyms")
    public abstract Part toPart(PartDto partDto);
}
