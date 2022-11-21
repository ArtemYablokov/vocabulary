package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.SynonymOrAntonymStringHolder;
import com.yablokovs.vocabulary.model.Part;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DefinitionMapper.class /*PartMapper.class*/})
public abstract class PartMapper {

    @Mapping(ignore = true, target = "synonyms")
    @Mapping(ignore = true, target = "antonyms")
    public abstract Part toPart(PartDto partDto);


    @Mapping(ignore = true, target = "antonyms")
    @Mapping(source = "synonyms", target = "synonyms", qualifiedBy = SynonymsQualifier.class)
    public abstract PartDto toPartDto(Part part);

    @SynonymsQualifier
    public List<SynonymOrAntonymStringHolder> mapSynonyms(List<Part> synonyms) {
        return synonyms.stream().map(s -> new SynonymOrAntonymStringHolder(s.getWord().getName())).collect(Collectors.toList());
    }
}
