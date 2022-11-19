package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void coupleSynonyms(WordRequest wordRequest, Word word) {

        // TODO: 03.11.2022 corner case when there is no synonyms
        Map<String, Set<String>> partOfSpeechToSynonym = synonymService.preparePartToSynonymMap(wordRequest);

        Set<String> collectSynonyms = partOfSpeechToSynonym.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Set<Word> allWordsWithPartsBySynonymsStrings = wordService.findAllWordsWithPartsBySynonymsStrings(collectSynonyms);

        Map<String, List<Long>> existedSynonyms = synonymService.getExistedSynonymsIds(partOfSpeechToSynonym, allWordsWithPartsBySynonymsStrings);
        List<Word> wordsToBeAddedWithNewParts = synonymService.getWordsToBeAddedWithNewParts(partOfSpeechToSynonym, allWordsWithPartsBySynonymsStrings);
        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(partOfSpeechToSynonym, allWordsWithPartsBySynonymsStrings);

        List<Word> mergedNewWordsToBeSaved = synonymService.mergeNewWordsToBeSaved(wordsToBeAddedWithNewParts, wordsToBeCreated);
        List<Word> savedWords = wordService.saveAllWords(mergedNewWordsToBeSaved);
        Map<String, List<Long>> newSynonymsPartIds = synonymService.getNewPartIdsFromSavedWords(savedWords, partOfSpeechToSynonym);

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
