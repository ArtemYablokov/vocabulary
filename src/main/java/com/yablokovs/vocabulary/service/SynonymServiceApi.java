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
    private final SynonymUtilService synonymUtilService; // TODO: 02/03/23 remove util from here
    private final WordService wordService;

    public SynonymServiceApi(SynonymService synonymService, SynonymUtilService synonymUtilService, WordService wordService) {
        this.synonymService = synonymService;
        this.synonymUtilService = synonymUtilService;
        this.wordService = wordService;
    }

    // TODO: 03.11.2022 corner case when there is no SYN for one of the PART
    // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService

    public void coupleSynAndAntNewImplementation(WordFrontEnd wordFrontEnd, Word word) {

        // возможно не нужны разделенные на 2 мапы...
        Map<String, Collection<String>> basicSynonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        Map<String, Collection<String>> basicAntonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);

        Map<String, Collection<String>> mergedSynonymsAndAntonyms = merge2maps(basicSynonymsMap, basicAntonymsMap);

        // NOT IMPORTANT maybe split to 2 calls
        Set<Word> existedSynonymsAndAntonymsFromRepo =
                wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(mergedSynonymsAndAntonyms));

        Map<String, Collection<Part>> existedSynonyms = synonymService.getExistedParts(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, Collection<Part>> existedAntonyms = synonymService.getExistedParts(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // 0 LVL of Syn / Ant
        Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms = getSetOfExistedSynonyms(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsToBeCoupledAsSynonyms = getSetOfExistedSynonyms(existedAntonyms);

        // move inside getSetOfExistedSynonyms
        Map<String, Collection<Set<Long>>> uniqueSetsOfExistedSynonyms = filterToUniqueSets(setsOfExistedSynonymsToBeCoupledAsSynonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfExistedAntonyms = filterToUniqueSets(setsOfExistedAntonymsToBeCoupledAsSynonyms);

        // 1 LVL of Ant
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfSynonyms = getSetOfExistedAntonyms(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfAntonyms = getSetOfExistedAntonyms(existedAntonyms);
        // TODO: 04/03/23 1 antonyms 2 ant of ant to Syn 3 Syn

        // STEP 2 added Ant of Ant to Syn
        Map<String, Collection<Set<Long>>> existedSynonymsToBeCoupledAsSynonyms = merge2maps(uniqueSetsOfExistedSynonyms, setsOfExistedAntonymsOfAntonyms);
        Map<String, Collection<Set<Long>>> existedAntonymsToBeCoupledAsSynonyms = merge2maps(uniqueSetsOfExistedAntonyms, setsOfExistedAntonymsOfSynonyms);

        Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsToBeCoupledAsSynonyms = filterToUniqueSets(existedSynonymsToBeCoupledAsSynonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsToBeCoupledAsSynonyms = filterToUniqueSets(existedAntonymsToBeCoupledAsSynonyms);

        // TODO: 05/03/23 сюда приходят uniqueSetsOfAntonymsToBeCoupledAsSynonyms (похоже после merge) непустое и пустое множеств (оно не нужно)
        coupleExistedANTandSYNasANT(synonymUtilService.coupleExistedANTandSYNAsAnt(uniqueSetsOfSynonymsToBeCoupledAsSynonyms,
                uniqueSetsOfAntonymsToBeCoupledAsSynonyms));

        Map<String, List<Long>> newSynonyms = getNewSynOrAntPartIds(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, List<Long>> newSynonymsWithWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(word, newSynonyms);

        Set<Word> existedANTFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicAntonymsMap));
        Map<String, List<Long>> newAntonyms = getNewSynOrAntPartIds(basicAntonymsMap, existedANTFromRepo);


        coupleExistedAndNewAsAntonyms(uniqueSetsOfSynonymsToBeCoupledAsSynonyms, uniqueSetsOfAntonymsToBeCoupledAsSynonyms, newSynonymsWithWord, newAntonyms);

        coupleAllSynonyms(uniqueSetsOfSynonymsToBeCoupledAsSynonyms, uniqueSetsOfAntonymsToBeCoupledAsSynonyms, newSynonymsWithWord, newAntonyms);
    }

    private void coupleExistedANTandSYNasANT(List<IdTuple> synonymUtilService) {
        // STEP 1 coupled Existed ANT & SYN as ANT
        // TODO: 05/03/23 coupleExistedANTandSYNAsAnt - check behavior of METHOD
        List<IdTuple> coupledExistedSynAndExistedAntAsAnt = synonymUtilService;
        synonymService.coupleAntonymsIds(coupledExistedSynAndExistedAntAsAnt);
    }

    private void coupleAllSynonyms(Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsToBeCoupledAsSynonyms,
                                   Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsToBeCoupledAsSynonyms,
                                   Map<String, List<Long>> newSynonymsWithWord,
                                   Map<String, List<Long>> newAntonyms) {
        // TODO: 05/03/23 BUG A & B not coupled as SYNONYMS
        // STEP 3 ALL SYNONYMS
        List<IdTuple> coupledAllNewSyns = synonymService.coupleSynonyms(uniqueSetsOfSynonymsToBeCoupledAsSynonyms, newSynonymsWithWord);
        List<IdTuple> coupledAllNewAnts = synonymService.coupleSynonyms(uniqueSetsOfAntonymsToBeCoupledAsSynonyms, newAntonyms);
        synonymService.coupleSynonymsIds(coupledAllNewSyns);
        synonymService.coupleSynonymsIds(coupledAllNewAnts);
    }


    private void coupleExistedAndNewAsAntonyms(Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsToBeCoupledAsSynonyms,
                                               Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsToBeCoupledAsSynonyms,
                                               Map<String, List<Long>> newSynonymsWithWord,
                                               Map<String, List<Long>> newAntonyms) {
        // STEP 1.2 coupled EX-NEW(х2) and NEW-NEW (as ANT)
        List<IdTuple> crossCoupledAsAntExistedSyn_NewAnt =
                synonymUtilService.crossCouple2Lists(synonymUtilService.flatMapNotDuplicatingSetsToOneSet(uniqueSetsOfSynonymsToBeCoupledAsSynonyms),
                        newAntonyms);
        List<IdTuple> crossCoupledAsAntExistedAnt_NewSyn =
                synonymUtilService.crossCouple2Lists(synonymUtilService.flatMapNotDuplicatingSetsToOneSet(uniqueSetsOfAntonymsToBeCoupledAsSynonyms),
                        newSynonymsWithWord);
        List<IdTuple> crossCoupledAsAntNewSyn_NewAnt = synonymUtilService.crossCouple2Lists(newSynonymsWithWord, newAntonyms);

        synonymService.coupleAntonymsIds(crossCoupledAsAntExistedSyn_NewAnt);
        synonymService.coupleAntonymsIds(crossCoupledAsAntExistedAnt_NewSyn);
        synonymService.coupleAntonymsIds(crossCoupledAsAntNewSyn_NewAnt);
    }


    private static Map<String, Collection<Set<Long>>> filterToUniqueSets(Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms) {
        return setsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                entry.getValue().stream().distinct().collect(Collectors.toList())));
    }

    private Map<String, Collection<Set<Long>>> getSetOfExistedSynonyms
            (Map<String, Collection<Part>> existedSynonyms) {
        return existedSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            Collection<Part> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                // TODO: 03/03/23 для антонимов будет пропускаться ID и выниматься будут антонимы
                Set<Long> synonymPartsId = new HashSet<>();
                synonymPartsId.add(part.getId());
                synonymPartsId.addAll(part.getSynonyms().stream().map(Part::getId).collect(Collectors.toSet()));
                synonymsId.add(synonymPartsId);
            });
            return synonymsId;
        }));
    }

    private Map<String, Collection<Set<Long>>> getSetOfExistedAntonyms(Map<String, Collection<Part>> existedSynonyms) {
        return existedSynonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {

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

    private <T> Map<String, Collection<T>> merge2maps(Map<String, Collection<T>> allAddedSynonyms, Map<String, Collection<T>> allAddedAntonyms) {
        Map<String, Collection<T>> merged = new HashMap<>(allAddedAntonyms);
        allAddedSynonyms.forEach((part, synonyms) -> {
            merged.merge(part, synonyms, (ant, syn) -> Stream.concat(ant.stream(), syn.stream()).collect(Collectors.toSet()));
        });

        return merged;
    }

    public Map<String, List<Long>> getNewSynOrAntPartIds
            (Map<String, Collection<String>> basicPartToSYNANTmap, Set<Word> existedSYNANTFromRepo) {

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

    private List<String> flatMapAllSynonymsToOneSet(Map<String, Collection<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).toList();
    }


    @Deprecated
    private Map<String, Word> getWordsToBeCreated(Map<String, Collection<String>> basicSynonymsMap, Map<String, Collection<Part>> existedSynonymsMap) {
        Map<String, Word> wordsToBeSaved = new HashMap<>();

        basicSynonymsMap.forEach((part, synonyms) -> {
            Collection<Part> existedSynonyms = existedSynonymsMap.get(part);

            if (!CollectionUtils.isEmpty(existedSynonyms)) {
                synonyms.forEach(s -> {
                    // TODO: 04/03/23  existedSynonymsMap switch to Set<Part>
                    boolean notExistSynonym = existedSynonyms.stream().noneMatch(existedSynonym -> existedSynonym.getWord().getName().equals(s));
                    if (notExistSynonym) {
                        Word word = wordsToBeSaved.get(s);
                        if (word != null) { // check if for different parts two equal words
                            word.addPart(new Part(part));
                        } else {
                            Word newWord = new Word(s);
                            newWord.addPart(new Part(part));
                            wordsToBeSaved.put(s, newWord);
                        }
                    }
                });
            }
        });
        return wordsToBeSaved;
    }

    @Deprecated
    public Map<String, Collection<Set<Long>>> getPartToExistedSynonymsNotDublicatingSets
            (Map<String, Collection<String>> basicPartToSynOrAntMap, Set<Word> existedSYNANTFromRepo) {
        // 4a existed WORDS with PARTS
        Map<String, List<Long>> partToExistedSYNids =
                synonymService.getExistedSynonymsIds(basicPartToSynOrAntMap, existedSYNANTFromRepo);
        // TODO: 03/03/23 выходит - здесь будет 2 реализации метода. 1 - возвращает основываясь на мапе синонов - SETы из синонимов (вместе с самим ID)
        //  2 - возвращает строго антонимы (основываясь на противоположной мапе) БЕЗ самого ID
        //  оба набора этих сетов надо передавать в след метод

        // 9 group SETs of existed SYNs to be coupled - retrieved from DB
        // OUTPUT part -> <abc>, <xyz>
        Map<String, Collection<Set<Long>>> partToExistedSynonymsNotDublicatingSets =
                synonymService.filterExistedSynonymsToUniqueSets(basicPartToSynOrAntMap.keySet(), partToExistedSYNids);

        return partToExistedSynonymsNotDublicatingSets;
    }

    /**
     * WordFrontEnd - is simplified word, where Synonyms and Antonyms in PART are only STRINGS - not complete Parts
     * word - word w/out syn/ant saved to DB, with assigned IDs
     * <p>
     * wordFrontEnd, synonymsOrAntonymsRetriever -> passed to build basic MAP
     * word -> passed to couple word's PARTs IDs with others
     * <p>
     * databaseName -> only to define to which TABLE save IDs
     */
//    @Deprecated
//    public void coupleSynAndAnt(WordFrontEnd wordFrontEnd, Word word) {
//
//        // 1 MAP PART -> SYNs
//        Map<String, Collection<String>> basicPartToSYNmap =
//                synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
//        // 3 EXISTED SYNs from DB
//        Set<Word> existedSYNFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToSYNmap));
//
//        Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToSYNmap, existedSYNFromRepo);
//        Map<String, List<Long>> newSynonyms = getNewSynOrAntPartIds(basicPartToSYNmap, existedSYNFromRepo);
//        Map<String, List<Long>> newSynonymsWithWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(word, newSynonyms);
//
//        // TODO: 28/02/23 блоки отличаются только по PartDto::getSynonyms
//
//        // 1 MAP PART -> SYNs
//        Map<String, Collection<String>> basicPartToANTmap =
//                synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);
//        // 3 EXISTED SYNs from DB
//        Set<Word> existedANTFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToANTmap));
//
//        // TODO: 28/02/23 мне кажется здесь всегда вызов по синонимам  -- ПРОВЕРИТЬ
//        Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToANTmap, existedANTFromRepo);
//        Map<String, List<Long>> newAntonyms = getNewSynOrAntPartIds(basicPartToANTmap, existedANTFromRepo);
//
//        List<IdTuple> synonymsToBeCoupled = synonymService.coupleSynonyms(existedSynonymsUniqueSets, newSynonymsWithWord);
//        List<IdTuple> antonymsToBeCoupledAsSynonyms = synonymService.coupleSynonyms(existedAntonymsUniqueSets, newAntonyms);
//        // 100 LAST step - simply couple IDs in MtoM table
//        synonymService.coupleSynonymsIds(synonymsToBeCoupled);
//        synonymService.coupleSynonymsIds(antonymsToBeCoupledAsSynonyms);
//
//        List<IdTuple> antonymsToBeCoupled = synonymService.coupleAntonyms(existedSynonymsUniqueSets, existedAntonymsUniqueSets, newSynonymsWithWord,
//                newAntonyms);
//        synonymService.coupleAntonymsIds(antonymsToBeCoupled);
//    }
}
