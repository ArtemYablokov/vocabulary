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

    public SynonymServiceApi(SynonymService synonymService, SynonymUtilService synonymUtilService, WordService wordService) {
        this.synonymService = synonymService;
        this.synonymUtilService = synonymUtilService;
        this.wordService = wordService;
    }

    public void coupleSynAndAntNewImplementation(WordFrontEnd wordFrontEnd, Word word) {

        Map<String, Collection<String>> basicSynonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        Map<String, Collection<String>> basicAntonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);

        Set<Word> existedSynonymsAndAntonymsFromRepo =
                wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(merge2maps(basicSynonymsMap, basicAntonymsMap)));

        Map<String, Collection<Part>> existedSynonyms = synonymService.getExistedParts(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, Collection<Part>> existedAntonyms = synonymService.getExistedParts(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsWithAntOfAnt = getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynonyms, existedAntonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsWithAntOfSyn = getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntonyms, existedSynonyms);

        Map<String, List<Long>> newSynonymsIncludingWord =
                synonymUtilService.addNewWordPartsToNewSynonymsPartIds(word, getNewSynOrAntPartIds(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo));
        Map<String, List<Long>> newAntonyms = getNewSynOrAntPartIds(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        List<IdTuple> antonymsToIdTuples =synonymService.coupleAntonymsToIdTuples(uniqueSetsOfSynonymsWithAntOfAnt, uniqueSetsOfAntonymsWithAntOfSyn,
                newSynonymsIncludingWord, newAntonyms);
        synonymService.coupleAntonymsIds(antonymsToIdTuples);

        // TODO: 06/03/23 service get as arg it's own output - not cool
        synonymService.coupleSynonymsIds(synonymService.coupleSynonymsToIdTuples(uniqueSetsOfSynonymsWithAntOfAnt, newSynonymsIncludingWord));
        synonymService.coupleSynonymsIds(synonymService.coupleSynonymsToIdTuples(uniqueSetsOfAntonymsWithAntOfSyn, newAntonyms));
    }

    private Map<String, Collection<Set<Long>>> getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(Map<String, Collection<Part>> existedSynonyms,
                                                                                      Map<String, Collection<Part>> existedAntonyms) {
        Map<String, Collection<Set<Long>>> setsOfExistedSynonyms = mapSetOfExistedSynonymsPartToId(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfAntonyms = mapSetOfExistedAntonymsPartToId(existedAntonyms);
        Map<String, Collection<Set<Long>>> existedSynonymsToBeCoupledAsSynonyms = merge2maps(setsOfExistedSynonyms, setsOfExistedAntonymsOfAntonyms);
        return filterToUniqueSets(existedSynonymsToBeCoupledAsSynonyms);
    }

    // TODO: 06/03/23 UTIL
    private static Map<String, Collection<Set<Long>>> filterToUniqueSets(Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms) {
        return setsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                entry.getValue().stream().distinct().collect(Collectors.toList())));
    }


    private Map<String, Collection<Set<Long>>> mapSetOfExistedSynonymsPartToId(Map<String, Collection<Part>> existedSynonyms) {
        return existedSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            Collection<Part> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                Set<Long> synonymPartsId = new HashSet<>();
                synonymPartsId.add(part.getId());
                synonymPartsId.addAll(part.getSynonyms().stream().map(Part::getId).collect(Collectors.toSet()));
                synonymsId.add(synonymPartsId);
            });
            return synonymsId;
        }));
    }

    private Map<String, Collection<Set<Long>>> mapSetOfExistedAntonymsPartToId(Map<String, Collection<Part>> existedAntonyms) {
        return existedAntonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {

            Collection<Part> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                List<Part> antonyms = part.getAntonyms();
                if (!CollectionUtils.isEmpty(antonyms)) {
                    synonymsId.add(antonyms.stream().map(Part::getId).collect(Collectors.toSet()));
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

    public Map<String, List<Long>> getNewSynOrAntPartIds(Map<String, Collection<String>> basicPartToSYNANTmap, Set<Word> existedSYNANTFromRepo) {

        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(basicPartToSYNANTmap, existedSYNANTFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(basicPartToSYNANTmap, existedSYNANTFromRepo);

        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);

        return synonymService.getPartIdsFromSavedWords(basicPartToSYNANTmap, Stream.concat(savedWords.stream(), updatedWords.stream()).toList());
    }

    private List<String> flatMapAllSynonymsToOneSet(Map<String, Collection<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).toList();
    }
}
