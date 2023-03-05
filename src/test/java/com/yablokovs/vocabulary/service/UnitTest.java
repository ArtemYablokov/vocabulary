package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.model.Word;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UnitTest {


    SynonymService synonymService = new SynonymService(null, null);

    @Test
    void getWordsToBeCreated() {

        Map<String, Collection<String>> partToSYNmap = new HashMap<>();
        Set<Word> existedWordsFromRepo = new HashSet<>();

        // both noun and verb SYNs hold same word
        List<String> verbs = List.of("verb1", "verb2", "verb3");
        List<String> nouns = List.of("noun1", "noun2", "verb3");
        partToSYNmap.put("verb", verbs);
        partToSYNmap.put("nouns", nouns);

        List<Word> partToWordsToBeCreated = synonymService.getWordsToBeCreated(partToSYNmap, existedWordsFromRepo);

        int n = 0;

    }

    @Test
    void nullForEach() {
        Function<PartDto, List<StringHolder>> getAntonyms = PartDto::getAntonyms;

        Function<PartDto, List<StringHolder>> getSynonyms = PartDto::getSynonyms;

        boolean b = getAntonyms == getSynonyms;
        boolean d = getAntonyms.equals(getSynonyms);

        Map<String, Collection<String>> stringListMap = new HashMap<>();
        Map<String, Collection<Set<Long>>> stringCollectionHashMap = new HashMap<>();
        handleAnyCollection(stringListMap);

        Map<String, List<Set<Long>>> uniqueSetsOfExistedSynonymsToBeCoupledAsSynonyms = new HashMap<>();
        uniqueSetsOfExistedSynonymsToBeCoupledAsSynonyms.put("a", List.of(Set.of(1L, 2L), Set.of(2L, 1L), Set.of(1L, 2L, 3L), Set.of(1L, 3L)));


        Map<String, List<Set<Long>>> collect = uniqueSetsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                entry.getValue().stream().distinct().collect(Collectors.toList())));


        List<String> strings = null;
        Assert.assertThrows(NullPointerException.class, () -> strings.forEach(System.out::println));
    }


    private <T> void handleAnyCollection(Map<String, Collection<T>> map) {
        map.forEach((key, valye) -> System.out.println(valye));
    }
}
