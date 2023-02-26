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
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.crossSetsToCoupleEachMemberOfSet(partToExistedSynonymsUniqueSets);
        Set<IdTuple> existedSynonymsToNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynOrAntPartIds);
        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynOrAntPartIds);

        synonymService.coupleSynonymsIds(existedSynonymsToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(existedSynonymsToNewToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(newSynonymsToBeCoupled, databaseName);
    }

    // TODO: 03.11.2022 corner case when there is no SYN for one of the PART

    /**
     WordFrontEnd - is simplified word, where Synonyms and Antonyms in PART are only STRINGS - not complete Parts
     word - word w/out syn/ant saved to DB, with assigned IDs

     wordFrontEnd, synonymsOrAntonymsRetriever -> passed to build basic MAP
     word -> passed to couple word's PARTs IDs with others

     databaseName -> only to define to which TABLE save IDs
     */
    public void coupleAntonymsNewWordFromRequest(WordFrontEnd wordFrontEnd,
                                                 Word word,
                                                 Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever,
                                                 DatabaseName databaseName) {

        // 1 MAP PART -> SYNs
        Map<String, Set<String>> basicPartToSYNmap = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, synonymsOrAntonymsRetriever);

        // 2 SYNs to COLLECTION
        Set<String> combinedSynOrAntFromAllParts = basicPartToSYNmap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        // 3 EXISTED SYNs
        Set<Word> existedWordsFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(combinedSynOrAntFromAllParts);

        // 4a  WORDS to be created
        List<Word> wordsToBeCreated = synonymService.getWordsToBeCreated(basicPartToSYNmap, existedWordsFromRepo);
        // 4a WORDS with NEW PARTS
        List<Word> wordsToBeUpdatedWithNewParts = synonymService.getWordsToBeUpdatedWithNewParts(basicPartToSYNmap, existedWordsFromRepo);

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
        // главная ошибка здесь - это опять фильтрация слов по частям речи -
        // не выйдет сгруппировать по частям речи
        // -> т.к. если есть два одинаковых слова в разных частях речи - то как их сохранять на шаге 5а???
        // -> ведь придет два одинаковых слова в каждой их частей речи (можно все это переложить в мапу перед сохранением и сделать слияние слов с разными PARTs)
        Map<String, List<Long>> newSynOrAntPartIds = synonymService.getNewPartIdsFromSavedWords(savedNewParts, basicPartToSYNmap);

        // ЗАМЕТКА ОБ АНТОНИМАХ
        // получается к этому моменту есть мапа PART - все ID от новосохраненных PART (будь то совсем новые WORD или добавленные PART)
        // теперь надо связать их все межу собой - синонимы,
        // а потом добавить к каждому из них новое слово - антоним

        // 8 add NEW WORD itself to PARTs IDs to be created
        // TODO: 20.11.2022 remove ALL COLLECTION MODIFICATION to helper service - to ease a burden on SynonymService
        // TODO: 26/02/23 rephrase - remove all CLEAN FUNCTION to UTIL SERVICE
        boolean antonym = false;
        if (!antonym) {
            synonymService.addNewWordPartsToNewSynonymsPartIds(word, newSynOrAntPartIds);
        }
        // для антонимов - не нужно созданное слово добавлять в новые PARTs
        // все антонимы (новые и старые) связываются в синонимы
        // TODO: 26/02/23 добавляется только шаг - добавить ко всем антонимам новым и старым текущее слово ()

        // к этому моменту есть ID от PART которые новые
        // Map<String, List<Long>> newSynOrAntPartIds
        // так же существующие
        // Map<String, List<Long>> partToExistedSYNids

        // проверить что происходит связь существующих синонимов например a(b) z(x) должны связаться a-z a-x b-z b-x
        // (ниже первого уровня не нужно уходить - так как все существующие синонимы связаны между собой)
        // 4a existed WORDS with PARTS
        Map<String, List<Long>> partToExistedSYNids = synonymService.getExistedSynonymsIds(basicPartToSYNmap, existedWordsFromRepo);

        // 9 group SETs of existed SYNs to be coupled - retrieved from DB
        // OUTPUT part -> <abc>, <xyz>
        Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets = synonymService.filterExistedSynonymsToUniqueSets(basicPartToSYNmap.keySet(), partToExistedSYNids, databaseName);

        // 10 FLATMAP of separated SETs to one SET
        // OUTPUT part -> <abcxyz>
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsNotDublicatingSets);


        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        // 11a couple existed SETs among
        // a-x a-y a-z, b = same, c = same
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.crossSetsToCoupleEachMemberOfSet(partToExistedSynonymsNotDublicatingSets);
        // 11b couple all existed with all new
        // each abcxyz with each new SYNs
        Set<IdTuple> existedSynonymsWithNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynOrAntPartIds);
        // 11c couple all new among each other
        // simply all NEW SYNs among each other
        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynOrAntPartIds);

        // 100 LAST step - simply couple IDs in MtoM table
        synonymService.coupleSynonymsIds(existedSynonymsToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(existedSynonymsWithNewToBeCoupled, databaseName);
        synonymService.coupleSynonymsIds(newSynonymsToBeCoupled, databaseName);
    }
}
