package com.yablokovs.vocabulary.model;

import java.util.List;

public interface PartAndWordRus {

    Long getId();
    List<? extends PartAndWordRus> getSynonyms();
    List<? extends PartAndWordRus> getAntonyms();
}
