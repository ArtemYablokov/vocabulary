package com.yablokovs.vocabulary.service.rus;

import com.yablokovs.vocabulary.model.WordRus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordRusService {

    private final WordRusDao wordRusDao;
    private final WordRusServiceFirstLayer wordRusServiceFirstLayer;

    public WordRusService(WordRusDao wordRusDao, WordRusServiceFirstLayer wordRusServiceFirstLayer) {
        this.wordRusDao = wordRusDao;
        this.wordRusServiceFirstLayer = wordRusServiceFirstLayer;
    }

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


    public Map<String, List<Long>> getNewSyn_AntWordRusIds(Map<String, Collection<String>> basicRusSynMap, Set<WordRus> wordsFromRepo) {
        List<WordRus> wordsToBeCreated = wordRusServiceFirstLayer.getWordsToBeCreated(basicRusSynMap, wordsFromRepo);
        List<WordRus> wordsCreated = wordRusDao.saveAll(wordsToBeCreated);
        return wordRusServiceFirstLayer.getWordRusIdsFromSavedWords(basicRusSynMap, wordsCreated);
    }
}
