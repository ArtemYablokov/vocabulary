package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yablokovs.vocabulary.repo.SynonymsRepo.DatabaseName;


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

    public void coupleSynOrAntsForNewWordFromRequest(WordFrontEnd wordFrontEnd,
                                                     Word word,
                                                     Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever,
                                                     DatabaseName databaseName) {

        // TODO: 03.11.2022 corner case when there is no synonyms
        Map<String, Set<String>> partOfSpeechToSynOrAnt = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, synonymsOrAntonymsRetriever);

        Set<String> unitedSynOrAntFromAllParts = partOfSpeechToSynOrAnt.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        // проверить что сюда приходят WORDS - только с PARTS???
        // нет - приходит вся глубина вложенности...
        Set<Word> wordsFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(unitedSynOrAntFromAllParts);

        Map<String, List<Long>> existedSynOrAnts = synonymService.getExistedSynonymsIds(partOfSpeechToSynOrAnt, wordsFromRepo);

        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(partOfSpeechToSynOrAnt, wordsFromRepo);

        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(partOfSpeechToSynOrAnt, wordsFromRepo);

        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);


        List<Word> wordsToGetNewPartIdsFrom = new ArrayList<>();
        wordsToGetNewPartIdsFrom.addAll(savedWords);
        wordsToGetNewPartIdsFrom.addAll(updatedWords);

        Map<String, List<Long>> newSynOrAntPartIds = synonymService.getNewPartIdsFromSavedWords(wordsToGetNewPartIdsFrom, partOfSpeechToSynOrAnt);


        // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService
        synonymService.addNewWordPartsToNewSynonymsPartIds(word, newSynOrAntPartIds);

        Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets = synonymService.filterExistedSynonymsToUniqueSets(partOfSpeechToSynOrAnt.keySet(), existedSynOrAnts, databaseName);
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsUniqueSets);

        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.getPairsOfExistedSynonymsToBeCoupled(partToExistedSynonymsUniqueSets);
        Set<IdTuple> existedSynonymsToNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynOrAntPartIds);
        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynOrAntPartIds);

        synonymService.coupleSynonymsIds(existedSynonymsToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(existedSynonymsToNewToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(newSynonymsToBeCoupled, databaseName);
    }

    // TODO: 03.11.2022 corner case when there is no SYN for one of the PART
    /**
    * WordFrontEnd - is simplified word, where Synonyms and Antonyms in PART are only STRINGS - not complete Parts
    *
    * */
    public void coupleAntonymsNewWordFromRequest(WordFrontEnd wordFrontEnd,
                                                     Word word,
                                                     Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever,
                                                     DatabaseName databaseName) {

        // 1 MAP PART -> SYNs
        Map<String, Set<String>> partToSYNmap = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, synonymsOrAntonymsRetriever);

        // 2 SYNs to COLLECTION
        Set<String> combinedSynOrAntFromAllParts = partToSYNmap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        // 3 EXISTED SYNs
        Set<Word> existedWordsFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(combinedSynOrAntFromAllParts);


        Map<String, Word> nameToWordMap = existedWordsFromRepo.stream().collect(Collectors.toMap(Word::getName, Function.identity()));

        // 5 filter


        Map<String, List<Long>> existedSynOrAnts = synonymService.getExistedSynonymsIds(partToSYNmap, existedWordsFromRepo);
        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(partToSYNmap, existedWordsFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(partToSYNmap, existedWordsFromRepo);




        List<Word> savedWords = wordService.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordService.updateAllWords(wordsToBeUpdatedWithNewParts);

        List<Word> wordsToGetNewPartIdsFrom = new ArrayList<>();
        wordsToGetNewPartIdsFrom.addAll(savedWords);
        wordsToGetNewPartIdsFrom.addAll(updatedWords);

        Map<String, List<Long>> newSynOrAntPartIds = synonymService.getNewPartIdsFromSavedWords(wordsToGetNewPartIdsFrom, partToSYNmap);

        // получается к этому моменту есть мапа PART - все ID от новосохраненных PART (будь то совсем новые WORD или добавленные PART)
        // теперь надо связать их все межу собой - синонимы
        // а потом добавить к каждому из них новое слово - антоним


        // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService
        synonymService.addNewWordPartsToNewSynonymsPartIds(word, newSynOrAntPartIds);
        // по идее только эту строчку для АНТ надо поменять на обратную - а все остальное сохраняется

        Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets = synonymService.filterExistedSynonymsToUniqueSets(partToSYNmap.keySet(), existedSynOrAnts, databaseName);
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsUniqueSets);

        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.getPairsOfExistedSynonymsToBeCoupled(partToExistedSynonymsUniqueSets);
        Set<IdTuple> existedSynonymsToNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynOrAntPartIds);
        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynOrAntPartIds);

        synonymService.coupleSynonymsIds(existedSynonymsToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(existedSynonymsToNewToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(newSynonymsToBeCoupled, databaseName);
    }
}
