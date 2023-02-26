package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Word;
import org.junit.jupiter.api.Test;

import java.util.*;

public class UnitTest {

    SynonymService synonymService =  new SynonymService(null, null, null);

    @Test
    void getPartToWordsToBeCreated() {

        Map<String, Set<String>> partToSYNmap = new HashMap<>();
        Set<Word> existedWordsFromRepo = new HashSet<>();

        // both noun and verb SYNs hold same word
        Set<String> verbs = new HashSet<>(List.of("verb1", "verb2", "verb3"));
        Set<String> nouns = new HashSet<>(List.of("noun1", "noun2", "verb3"));
        partToSYNmap.put("verb", verbs);
        partToSYNmap.put("nouns", nouns);

        Map<String, Set<Word>> partToWordsToBeCreated = synonymService.getPartToWordsToBeCreated(partToSYNmap, existedWordsFromRepo);

        int n = 0;

    }
}
