package com.yablokovs.vocabulary.mdto.front.mapper;

import com.yablokovs.vocabulary.mdto.front.PartDto;
import com.yablokovs.vocabulary.model.Part;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DefinitionMapper.class})
public abstract class PartMapper {

    @Mapping(ignore = true, target = "antonyms")
    @Mapping(ignore = true, target = "synonyms")
    public abstract Part toPart(PartDto partDto);
}
