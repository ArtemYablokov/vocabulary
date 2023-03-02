package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * API pattern - to hide more complex implementation
 */
@Service
public class SynonymServiceApi {

    private final SynonymService synonymService;
    private final SynonymUtilService synonymUtilService; // TODO: 02/03/23 remove util from here
    private final WordService wordService;

    public SynonymServiceApi(SynonymService synonymService, SynonymUtilService synonymUtilService, WordService wordService) {
        this.synonymService = synonymService;
        this.synonymUtilService = synonymUtilService;
        this.wordService = wordService;
    }

    // TODO: 03.11.2022 corner case when there is no SYN for one of the PART
    // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService

    /**
     * WordFrontEnd - is simplified word, where Synonyms and Antonyms in PART are only STRINGS - not complete Parts
     * word - word w/out syn/ant saved to DB, with assigned IDs
     * <p>
     * wordFrontEnd, synonymsOrAntonymsRetriever -> passed to build basic MAP
     * word -> passed to couple word's PARTs IDs with others
     * <p>
     * databaseName -> only to define to which TABLE save IDs
     */
    public void coupleSynAndAnt(WordFrontEnd wordFrontEnd, Word word) {

        // 1 MAP PART -> SYNs
        Map<String, Set<String>> basicPartToSYNmap =
                synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        // 3 EXISTED SYNs from DB
        Set<Word> existedSYNFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToSYNmap));

        Map<String, List<Set<Long>>> existedSynonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToSYNmap, existedSYNFromRepo);
        Map<String, List<Long>> newSynonyms = getNewSynOrAntPartIds(basicPartToSYNmap, existedSYNFromRepo);
        Map<String, List<Long>> newSynonymsWithWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(word, newSynonyms);

        // TODO: 28/02/23 блоки отличаются только по PartDto::getSynonyms

        // 1 MAP PART -> SYNs
        Map<String, Set<String>> basicPartToANTmap =
                synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);
        // 3 EXISTED SYNs from DB
        Set<Word> existedANTFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToANTmap));

        // TODO: 28/02/23 мне кажется здесь всегда вызов по синонимам  -- ПРОВЕРИТЬ
        Map<String, List<Set<Long>>> existedAntonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToANTmap, existedANTFromRepo);
        Map<String, List<Long>> newAntonyms = getNewSynOrAntPartIds(basicPartToANTmap, existedANTFromRepo);

        List<IdTuple> synonymsToBeCoupled = synonymService.coupleSynonyms(existedSynonymsUniqueSets, newSynonymsWithWord);
        List<IdTuple> antonymsToBeCoupledAsSynonyms = synonymService.coupleSynonyms(existedAntonymsUniqueSets, newAntonyms);
        // 100 LAST step - simply couple IDs in MtoM table
        synonymService.coupleSynonymsIds(synonymsToBeCoupled);
        synonymService.coupleSynonymsIds(antonymsToBeCoupledAsSynonyms);

        List<IdTuple> antonymsToBeCoupled = synonymService.coupleAntonyms(existedSynonymsUniqueSets, existedAntonymsUniqueSets, newSynonymsWithWord, newAntonyms);
        synonymService.coupleAntonymsIds(antonymsToBeCoupled);
    }

    public Map<String, List<Set<Long>>> getPartToExistedSynonymsNotDublicatingSets(Map<String, Set<String>> basicPartToSYNANTmap, Set<Word> existedSYNANTFromRepo) {
        // 4a existed WORDS with PARTS
        Map<String, List<Long>> partToExistedSYNids =
                synonymService.getExistedSynonymsIds(basicPartToSYNANTmap, existedSYNANTFromRepo);

        // 9 group SETs of existed SYNs to be coupled - retrieved from DB
        // OUTPUT part -> <abc>, <xyz>
        Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets =
                // TODO: 28/02/23 мне кажется здесь всегда вызов по синонимам... ну да. дальше результат передается только на связь синонимов...
                synonymService.filterExistedSynonymsToUniqueSets(basicPartToSYNANTmap.keySet(), partToExistedSYNids);

        return partToExistedSynonymsNotDublicatingSets;
    }

    public Map<String, List<Long>> getNewSynOrAntPartIds(Map<String, Set<String>> basicPartToSYNANTmap, Set<Word> existedSYNANTFromRepo) {

        // 4a  WORDS to be created
        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(basicPartToSYNANTmap, existedSYNANTFromRepo);
        // 4a WORDS with NEW PARTS
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(basicPartToSYNANTmap, existedSYNANTFromRepo);

        // 5a save NEW WORDS - to get PART ids later
        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        // 5a save WORDS with NEW PARTS - to get PART ids later
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);
        // 6 collect ALL new PART ids
        // both - created WORDs and added new PARTs to existed WORDs -> handled same as new PARTS
        List<Word> savedNewParts = new ArrayList<>();
        savedNewParts.addAll(savedWords);
        savedNewParts.addAll(updatedWords);

        // 7 PART ids to be created by PART
        Map<String, List<Long>> newSynOrAntPartIds =
                synonymService.getPartIdsFromSavedWords(basicPartToSYNANTmap, savedNewParts);

        return newSynOrAntPartIds;
    }

    public Set<String> flatMapAllSynonymsToOneSet(Map<String, Set<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

}
