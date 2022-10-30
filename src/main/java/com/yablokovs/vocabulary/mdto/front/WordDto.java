package com.yablokovs.vocabulary.mdto.front;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter @Setter
public class WordDto {

    private String name;

    private List<PartDto> parts;

}
