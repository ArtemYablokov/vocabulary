package com.yablokovs.vocabulary.mdto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class WordFrontEnd {

    private String name;

    private List<PartDto> parts;

}
