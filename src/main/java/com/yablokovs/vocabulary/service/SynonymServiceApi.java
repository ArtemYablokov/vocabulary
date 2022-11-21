package com.yablokovs.vocabulary.service;

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
    private final WordService wordService;

    public SynonymServiceApi(SynonymService synonymService, WordService wordService) {
        this.synonymService = synonymService;
        this.wordService = wordService;
    }

    public void coupleSynonymsForNewWordFromRequest(WordFrontEnd wordFrontEnd, Word word) {

        // TODO: 03.11.2022 corner case when there is no synonyms
        Map<String, Set<String>> partOfSpeechToSynonym = synonymService.getAllSynonymsStringSortedByPartOfSpeech(wordFrontEnd);

        Set<String> unitedSynonymsFromAllParts = partOfSpeechToSynonym.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        // проверить что сюда приходят WORDS - только с PARTS???
        // нет - приходит вся глубина вложенности...
        Set<Word> wordsFromRepo = wordService.findAllWordsWithPartsBySynonymsStrings(unitedSynonymsFromAllParts);

        Map<String, List<Long>> existedSynonyms = synonymService.getExistedSynonymsIds(partOfSpeechToSynonym, wordsFromRepo);

        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(partOfSpeechToSynonym, wordsFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(partOfSpeechToSynonym, wordsFromRepo);

        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);


        List<Word> wordsToGetNewPartIdsFrom = new ArrayList<>();
        wordsToGetNewPartIdsFrom.addAll(savedWords);
        wordsToGetNewPartIdsFrom.addAll(updatedWords);

        Map<String, List<Long>> newSynonymsPartIds = synonymService.getNewPartIdsFromSavedWords(wordsToGetNewPartIdsFrom, partOfSpeechToSynonym);

        // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService
        synonymService.addNewWordPartsToNewSynonymsPartIds(word, newSynonymsPartIds);

        Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets = synonymService.filterExistedSynonymsToUniqueSets(partOfSpeechToSynonym.keySet(), existedSynonyms);
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsUniqueSets);

        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.getPairsOfExistedSynonymsToBeCoupled(partToExistedSynonymsUniqueSets);
        Set<IdTuple> existedSynonymsToNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynonymsPartIds);
        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynonymsPartIds);

        synonymService.coupleIds(existedSynonymsToBeCoupled);
        synonymService.coupleIds(existedSynonymsToNewToBeCoupled);
        synonymService.coupleIds(newSynonymsToBeCoupled);
    }
}
