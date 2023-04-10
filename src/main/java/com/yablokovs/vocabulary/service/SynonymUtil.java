package com.yablokovs.vocabulary.service;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SynonymUtil {


    public Map<String, Collection<Set<Long>>> filterToUniqueSets(Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms) {
        return setsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet()
                .stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> entry.getValue()
                                        .stream()
                                        .distinct()
                                        .collect(Collectors.toList())));
    }

    // TODO: 06/03/23 UTIL
    public <T> Map<String, Collection<T>> merge2maps(Map<String, Collection<T>> allAddedSynonyms, Map<String, Collection<T>> allAddedAntonyms) {
        Map<String, Collection<T>> merged = new HashMap<>(allAddedAntonyms);
        allAddedSynonyms.forEach((part, synonyms) -> {
            merged.merge(part, synonyms, (ant, syn) -> Stream.concat(ant.stream(), syn.stream()).collect(Collectors.toSet()));
        });

        return merged;
    }
}
