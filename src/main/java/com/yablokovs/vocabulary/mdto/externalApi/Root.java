package com.yablokovs.vocabulary.mdto.externalApi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


// TODO: 21.10.2022 implement ignore unknown
@Setter
public class Root {
    @Getter
    private String word;
    private String phonetic;
    private List<Phonetic> phonetics;
    @Getter
    private List<Meaning> meanings;
    private License license;
    private List<String> sourceUrls;
}