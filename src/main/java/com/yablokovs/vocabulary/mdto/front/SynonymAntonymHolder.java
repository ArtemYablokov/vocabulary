package com.yablokovs.vocabulary.mdto.front;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Class just for Front representation of filled form
* */
@Getter
@Setter
@NoArgsConstructor
public class SynonymAntonymHolder {
    private String name;

    public SynonymAntonymHolder(String name) {
        this.name = name;
    }
}
