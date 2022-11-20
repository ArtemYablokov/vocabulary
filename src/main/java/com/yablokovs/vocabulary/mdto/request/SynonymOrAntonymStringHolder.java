package com.yablokovs.vocabulary.mdto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Class just for Front representation of filled form
* */
@Getter
@Setter
@NoArgsConstructor
public class SynonymOrAntonymStringHolder {
    private String name;

    public SynonymOrAntonymStringHolder(String name) {
        this.name = name;
    }
}
