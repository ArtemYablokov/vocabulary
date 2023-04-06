package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * API pattern - to hide more complex implementation
 */
@Service
public class SynonymServiceApi {

    private final SynonymService synonymService;
    private final SynonymUtilService synonymUtilService;
    private final WordService wordService;

    private final SynonymDAOService synonymDAOService;

    public SynonymServiceApi(SynonymService synonymService, SynonymUtilService synonymUtilService, WordService wordService, SynonymDAOService synonymDAOService) {
        this.synonymService = synonymService;
        this.synonymUtilService = synonymUtilService;
        this.wordService = wordService;
        this.synonymDAOService = synonymDAOService;
    }

    public void coupleSynAndAntNewImplementation(WordFrontEnd wordFrontEnd, Word word) {

        Map<String, Collection<String>> basicSynonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        Map<String, Collection<String>> basicAntonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);

        Set<Word> existedSynonymsAndAntonymsFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(merge2maps(basicSynonymsMap, basicAntonymsMap)));

        // EXISTED
        Map<String, Collection<Part>> existedSynonyms = synonymService.getExistedParts(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, Collection<Part>> existedAntonyms = synonymService.getExistedParts(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // EXISTED with ANT of ANT
        Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsWithAntOfAnt = getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynonyms, existedAntonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsWithAntOfSyn = getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntonyms, existedSynonyms);

        // NEW
        Map<String, List<Long>> newSynonymsIncludingWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(
                word, getNewSyn_AntPartIds(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo));
        Map<String, List<Long>> newAntonyms = getNewSyn_AntPartIds(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // PREPARED TUPLES
        List<IdTuple> antonymsToIdTuples = synonymService.coupleSynonymsAndAntonymsAsAntonyms(
                uniqueSetsOfSynonymsWithAntOfAnt, uniqueSetsOfAntonymsWithAntOfSyn, newSynonymsIncludingWord, newAntonyms);
        List<IdTuple> coupledSynonymsAsSynonyms = synonymService.coupleSynonymsToIdTuples(uniqueSetsOfSynonymsWithAntOfAnt, newSynonymsIncludingWord);
        List<IdTuple> coupledAntonymsAsSynonyms = synonymService.coupleSynonymsToIdTuples(uniqueSetsOfAntonymsWithAntOfSyn, newAntonyms);

        synonymDAOService.saveAntonymIdTuple(antonymsToIdTuples);
        synonymDAOService.saveSynonymIdTuple(coupledSynonymsAsSynonyms);
        synonymDAOService.saveSynonymIdTuple(coupledAntonymsAsSynonyms);
    }

    private Map<String, Collection<Set<Long>>> getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(
            Map<String, Collection<Part>> existedSynonyms, Map<String, Collection<Part>> existedAntonyms) {

        Map<String, Collection<Set<Long>>> setsOfExistedSynonyms = getSetsOfCoupledSynonyms(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfAntonyms = getSetsOfCoupledAntonyms(existedAntonyms);
        Map<String, Collection<Set<Long>>> existedSynonymsToBeCoupledAsSynonyms = merge2maps(setsOfExistedSynonyms, setsOfExistedAntonymsOfAntonyms);

        return filterToUniqueSets(existedSynonymsToBeCoupledAsSynonyms);
    }

    private static Map<String, Collection<Set<Long>>> filterToUniqueSets(Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms) {
        return setsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet()
                .stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> entry.getValue()
                                        .stream()
                                        .distinct()
                                        .collect(Collectors.toList())));
    }

    private Map<String, Collection<Set<Long>>> getSetsOfCoupledSynonyms(Map<String, Collection<Part>> existedSynonyms) {
        return existedSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            Collection<Part> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();

            listOfParts.forEach(part -> {
                Set<Long> synonymPartsId = new HashSet<>();
                synonymPartsId.add(part.getId()); // INCLUDING ITSELF
                synonymPartsId.addAll(part.getSynonyms().stream().map(Part::getId).collect(Collectors.toSet()));
                synonymsId.add(synonymPartsId);
            });
            return synonymsId;
        }));
    }

    private Map<String, Collection<Set<Long>>> getSetsOfCoupledAntonyms(Map<String, Collection<Part>> existedAntonyms) {
        return existedAntonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {

            Collection<Part> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                List<Part> antonyms = part.getAntonyms();
                if (!CollectionUtils.isEmpty(antonyms)) { // purposes - NOT TO ADD EMPTY SET
                    synonymsId.add(antonyms.stream().map(Part::getId).collect(Collectors.toSet())); // EXCLUDING ITSELF
                }
            });

            return synonymsId;
        }));
    }

    // TODO: 06/03/23 UTIL
    private <T> Map<String, Collection<T>> merge2maps(Map<String, Collection<T>> allAddedSynonyms, Map<String, Collection<T>> allAddedAntonyms) {
        Map<String, Collection<T>> merged = new HashMap<>(allAddedAntonyms);
        allAddedSynonyms.forEach((part, synonyms) -> {
            merged.merge(part, synonyms, (ant, syn) -> Stream.concat(ant.stream(), syn.stream()).collect(Collectors.toSet()));
        });

        return merged;
    }

    public Map<String, List<Long>> getNewSyn_AntPartIds(Map<String, Collection<String>> basicPartToSyn_AntMap, Set<Word> existedSyn_AntFromRepo) {

        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(basicPartToSyn_AntMap, existedSyn_AntFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(basicPartToSyn_AntMap, existedSyn_AntFromRepo);

        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);

        return synonymService.getPartIdsFromSavedWords(basicPartToSyn_AntMap, Stream.concat(savedWords.stream(), updatedWords.stream()).toList());
    }

    private List<String> flatMapAllSynonymsToOneSet(Map<String, Collection<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).toList();
    }
}
