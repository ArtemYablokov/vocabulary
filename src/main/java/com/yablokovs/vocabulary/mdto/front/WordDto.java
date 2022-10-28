package com.yablokovs.vocabulary.mdto.front;

import lombok.Data;

import java.util.List;


@Data
public class WordDto {

    private String name;

    private List<PartDto> parts;

}
