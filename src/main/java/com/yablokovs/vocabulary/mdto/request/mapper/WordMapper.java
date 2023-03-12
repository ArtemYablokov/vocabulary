package com.yablokovs.vocabulary.mdto.request.mapper;

import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PartMapper.class})
public abstract class WordMapper {

    public abstract Word mapRequestToWordIgnoreSynonymsAndAntonyms(WordFrontEnd wordFrontEnd);


    @Mapping(source = "tags", target = "tags")
    @Mapping(source = "updatedAt", target = "lastSearched", qualifiedBy = DateQualifier.class)
    @Mapping(source = "createdAt", target = "createdAt", qualifiedBy = DateQualifier.class)
    @Mapping(source = "parts", target = "partOfSpeechList", qualifiedBy = PartsQualifier.class)
    public abstract WordFrontEnd toWordResponse(Word word);

    @DateQualifier
    public String mapDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @PartsQualifier
    public String collectPartNames(Set<Part> parts) {
        return parts.stream().map(Part::getName).collect(Collectors.joining());
    }
}
