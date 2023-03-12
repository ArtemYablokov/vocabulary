package com.yablokovs.vocabulary.mdto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class WordFrontEnd {

    private String name;
    private Long numberOfSearches;
    private List<PartDto> parts;

    private List<StringHolder> tags;
    private String lastSearched;
    private String createdAt;
    private String partOfSpeechList;
}
