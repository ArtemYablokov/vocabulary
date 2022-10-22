package com.yablokovs.vocabulary.mdto.externalApi;

import lombok.Setter;

@Setter
public class Phonetic{
    private String text;
    private String audio;
    private String sourceUrl;
    private License license;
}
