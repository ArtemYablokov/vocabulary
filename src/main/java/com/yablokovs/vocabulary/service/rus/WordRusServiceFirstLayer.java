package com.yablokovs.vocabulary.service.rus;

import com.yablokovs.vocabulary.model.WordRus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordRusServiceFirstLayer {


    public List<WordRus> getWordsToBeCreated(Map<String, Collection<String>> basicRusSynMap, Set<WordRus> wordsFromRepo) {

        List<WordRus> wordsToBeSaved = new ArrayList<>();

        basicRusSynMap.forEach((partOfSpeech, synOrAnts) -> {

            synOrAnts.forEach(synOrAnt -> {
                boolean noneMatch = wordsFromRepo.stream().noneMatch(word -> word.getName().equals(synOrAnt));

                if (noneMatch) {
                    WordRus e = new WordRus();
                    e.setName(synOrAnt);
                    e.setPartOfSpeech(partOfSpeech);
                    wordsToBeSaved.add(e);
                }
            });
        });

        return wordsToBeSaved;
    }

    public Map<String, List<Long>> getWordRusIdsFromSavedWords(Map<String, Collection<String>> basicPartToSYNmap, List<WordRus> savedNewParts) {
        Map<String, List<Long>> newSyn_AntWordRusIds = new HashMap<>();

        basicPartToSYNmap.forEach((part, syn_ant) -> {
            List<Long> wordRusIds = new ArrayList<>();

            syn_ant.forEach(s ->
                    savedNewParts
                            .stream()
                            .filter(wordRus -> wordRus.getName().equals(s) && wordRus.getPartOfSpeech().equals(part))
                            .findAny()
                            .ifPresent(w -> wordRusIds.add(w.getId())));

            newSyn_AntWordRusIds.put(part, wordRusIds);
        });

        return newSyn_AntWordRusIds;
    }


}
