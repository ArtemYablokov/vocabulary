package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.model.PartAndWordRus;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class FirstLevelService {

    private final SynonymService synonymService;
    private final WordDao wordDao;
    private final SynonymUtil synonymUtil;

    public FirstLevelService(SynonymService synonymService, WordDao wordDao, SynonymUtil synonymUtil) {
        this.synonymService = synonymService;
        this.wordDao = wordDao;
        this.synonymUtil = synonymUtil;
    }


    public  <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(
            Map<String, Collection<T>> existedSynonyms, Map<String, Collection<T>> existedAntonyms) {

        Map<String, Collection<Set<Long>>> setsOfExistedSynonyms = getSetsOfCoupledSynonyms(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfAntonyms = getSetsOfCoupledAntonyms(existedAntonyms);
        Map<String, Collection<Set<Long>>> existedSynonymsToBeCoupledAsSynonyms = synonymUtil.merge2maps(setsOfExistedSynonyms, setsOfExistedAntonymsOfAntonyms);

        return synonymUtil.filterToUniqueSets(existedSynonymsToBeCoupledAsSynonyms);
    }

    private <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getSetsOfCoupledSynonyms(Map<String, Collection<T>> existedSynonyms) {

        return existedSynonyms.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            Collection<T> listOfParts = entry.getValue();
                            Collection<Set<Long>> synonymsId = new ArrayList<>();

                            listOfParts.forEach(part -> {
                                Set<Long> synonymPartsId = new HashSet<>();
                                synonymPartsId.add(part.getId()); // INCLUDING ITSELF
                                synonymPartsId.addAll(part.getSynonyms().stream().map(PartAndWordRus::getId).collect(Collectors.toSet()));
                                synonymsId.add(synonymPartsId);
                            });
                            return synonymsId;
                        }));
    }

    // TODO: 06/03/23 UTIL
    public Map<String, List<Long>> getNewSyn_AntPartIds(Map<String, Collection<String>> basicPartToSyn_AntMap, Set<Word> existedSyn_AntFromRepo) {

        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(basicPartToSyn_AntMap, existedSyn_AntFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(basicPartToSyn_AntMap, existedSyn_AntFromRepo);

        List<Word> savedWords = wordDao.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordDao.updateAllWords(wordsToBeUpdatedWithNewParts);

        return synonymService.getPartIdsFromSavedWords(basicPartToSyn_AntMap, Stream.concat(savedWords.stream(), updatedWords.stream()).toList());
    }

    private <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getSetsOfCoupledAntonyms(Map<String, Collection<T>> existedAntonyms) {
        return existedAntonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {

            Collection<T> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                List<? extends PartAndWordRus> antonyms = part.getAntonyms();
                if (!CollectionUtils.isEmpty(antonyms)) { // purposes - NOT TO ADD EMPTY SET
                    synonymsId.add(antonyms.stream().map(PartAndWordRus::getId).collect(Collectors.toSet())); // EXCLUDING ITSELF
                }
            });

            return synonymsId;
        }));
    }




}
