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
public class StringHolder {
    private String name;

    public StringHolder(String name) {
        this.name = name;
    }
}
