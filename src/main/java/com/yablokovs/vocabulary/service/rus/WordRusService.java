package com.yablokovs.vocabulary.service.rus;

import com.yablokovs.vocabulary.model.WordRus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordRusService {


    // TODO: 4/8/23 UTIL
    public Map<String, Collection<WordRus>> getExistedWordRus(Map<String, Collection<String>> basicPartToSyn_AntMap, Set<WordRus> wordsFromRepo) {
        Map<String, Collection<WordRus>> partToExistedSynOrAntIds = new HashMap<>();

        basicPartToSyn_AntMap.forEach((partOfSpeech, synOrArts) -> {
            List<WordRus> existedSynOrAntIds = new ArrayList<>();

            synOrArts.forEach(synOrAnt -> {
                wordsFromRepo
                        .stream()
                        .filter(w -> w.getName().equals(synOrAnt) && w.getPartOfSpeech().equals(partOfSpeech))
                        .findAny()
                        .ifPresent(existedSynOrAntIds::add);
            });
            partToExistedSynOrAntIds.put(partOfSpeech, existedSynOrAntIds);
        });

        return partToExistedSynOrAntIds;
    }
}
