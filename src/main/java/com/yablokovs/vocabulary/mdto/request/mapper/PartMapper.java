package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
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

    @Mapping(source = "synonyms", target = "synonyms", qualifiedBy = SynonymsQualifier.class)
    @Mapping(source = "antonyms", target = "antonyms", qualifiedBy = AntonymsQualifier.class)
    public abstract PartDto toPartDto(Part part);

    @SynonymsQualifier
    public List<StringHolder> mapSynonyms(List<Part> synonyms) {
        return synonyms.stream().map(s -> new StringHolder(s.getWord().getName())).collect(Collectors.toList());
    }

    @AntonymsQualifier
    public List<StringHolder> mapAntonyms(List<Part> synonyms) {
        return synonyms.stream().map(s -> new StringHolder(s.getWord().getName())).collect(Collectors.toList());
    }
}
