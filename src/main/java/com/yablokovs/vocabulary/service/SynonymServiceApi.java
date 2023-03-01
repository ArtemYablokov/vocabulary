package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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
                synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, PartDto::getSynonyms);
        // 3 EXISTED SYNs from DB
        Set<Word> existedSYNFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToSYNmap));

        Map<String, List<Set<Long>>> existedSynonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToSYNmap, existedSYNFromRepo, DatabaseName.SYNONYM);
        Map<String, List<Long>> newSynonyms = getNewSynOrAntPartIds(basicPartToSYNmap, existedSYNFromRepo);

        // TODO: 28/02/23 да и в целом бллоки отличаются только по PartDto::getSynonyms

        // 1 MAP PART -> SYNs
        Map<String, Set<String>> basicPartToANTmap =
                synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, PartDto::getSynonyms);
        // 3 EXISTED SYNs from DB
        Set<Word> existedANTFromRepo = wordService.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(basicPartToANTmap));
        // TODO: 28/02/23 мне кажется здесь всегда вызов по синонимам  -- ПРОВЕРИТЬ
        Map<String, List<Set<Long>>> existedAntonymsUniqueSets = getPartToExistedSynonymsNotDublicatingSets(basicPartToANTmap, existedANTFromRepo, DatabaseName.ANTONYM);
        Map<String, List<Long>> newAntonyms = getNewSynOrAntPartIds(basicPartToANTmap, existedANTFromRepo);

        Map<String, List<Long>> newSynonymsPartIdsWithWord = addNewWordPartsToNewSynonymsPartIds(word, newSynonyms);

        coupleSynonyms(existedSynonymsUniqueSets, newSynonymsPartIdsWithWord);
        coupleSynonyms(existedAntonymsUniqueSets, newAntonyms);

        // 1 новые и старые SYN пересекать с WORD уже не нужно

        // 2 а вот для антонимов имеет смысл пересечь WORD с ними (как старыми, так и новыми)
        // 3 тогда если положить слово в NEW SYN - оно будет связано и со старыми, и с новыми ANT (а оно уже там и лежит :) )

        // связь (-)
        coupleExistedANTandSYN(existedSynonymsUniqueSets, existedAntonymsUniqueSets);

        crossCouple2ListsOfId(newSynonymsPartIdsWithWord, newAntonyms);
        crossCouple2ListsOfId(flatMapNotDuplicatingSetsToOneSet(existedSynonymsUniqueSets), newAntonyms);
        crossCouple2ListsOfId(flatMapNotDuplicatingSetsToOneSet(existedAntonymsUniqueSets), newSynonymsPartIdsWithWord);
    }

    private void crossCouple2ListsOfId(Map<String, List<Long>> newSynonymsPartIdsWithWord, Map<String, List<Long>> newAntonyms) {
        // TODO: 01/03/23 тупое связывание всего подряд перекрестьем трижды - один метод

    }

    private void coupleExistedANTandSYN(Map<String, List<Set<Long>>> existedSynonymsUniqueSets, Map<String, List<Set<Long>>> existedAntonymsUniqueSets) {
        // важно ли с какой стороны смотреть? нет - тк с какой стороны не начни - будут проверены все пары множеств ->
        // тогда юзаем БД Антонимов - тк менее нагруженная XDDDD
    }

    public Map<String, List<Set<Long>>> getPartToExistedSynonymsNotDublicatingSets(Map<String, Set<String>> basicPartToSYNANTmap, Set<Word> existedSYNANTFromRepo, DatabaseName databaseName) {
        // 4a existed WORDS with PARTS
        Map<String, List<Long>> partToExistedSYNids =
                synonymService.getExistedSynonymsIds(basicPartToSYNANTmap, existedSYNANTFromRepo);

        // 9 group SETs of existed SYNs to be coupled - retrieved from DB
        // OUTPUT part -> <abc>, <xyz>
        Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets =
                // TODO: 28/02/23 мне кажется здесь всегда вызов по синонимам... ну да. дальше результат передается только на связь синонимов...
                synonymService.filterExistedSynonymsToUniqueSets(basicPartToSYNANTmap.keySet(), partToExistedSYNids, databaseName);

        System.out.println();
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
                synonymService.getNewPartIdsFromSavedWords(basicPartToSYNANTmap, savedNewParts);

        System.out.println();
        return newSynOrAntPartIds;
    }

    // TODO: 28/02/23 перестает иметь смысл - связь произойдет при связывании SYN & ANT
    /*
     * Need to use parts - because existed or new could miss one of the part
     * */
    private void crossWordAsANTtoAllAntonyms(Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew,
                                             Map<String, List<Long>> newSynOrAntPartIds, Word word, Set<String> parts) {

        Map<String, Set<Long>> existedAndNewIds = merge2collections(partToExistedSynonymsToBeCoupledWithNew, newSynOrAntPartIds, parts);

        List<IdTuple> wordToAntonymsToBeCoupled = crossWordToAllsAntonymsToBeCoupled(word, existedAndNewIds);

        synonymService.coupleAntonymsIds(wordToAntonymsToBeCoupled);
    }

    private Map<String, Set<Long>> merge2collections(Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> newSynOrAntPartIds, Set<String> parts) {
        Map<String, Set<Long>> existedAndNewIds = new HashMap<>();


        parts.forEach(part -> {
            Set<Long> existedIds = partToExistedSynonymsToBeCoupledWithNew.get(part);
            // TODO: 27/02/23 what if one of collection will be null ??? NPE ?
            Set<Long> summ = new HashSet<>(newSynOrAntPartIds.get(part));
            summ.addAll(existedIds);
            if (!CollectionUtils.isEmpty(summ)) {
                existedAndNewIds.put(part, summ);
            }
        });
        return existedAndNewIds;
    }

    private List<IdTuple> crossWordToAllsAntonymsToBeCoupled(Word word, Map<String, Set<Long>> existedAndNewIds) {
        List<IdTuple> wordToAntonymsToBeCoupled = new ArrayList<>();
        // TODO: 27/02/23 !!! можно идти наоборот от синонимов - и тогда не нужно проверять - ведь у сова точно существует PART (а вот наоборот - как сейчас возможно - поэтому проверяю)
        existedAndNewIds.forEach((partName, ids) -> {
            word.getParts().stream()
                    .filter(part -> part.getName().equals(partName))
                    .findAny()
                    // всегда будет present
                    .ifPresent(part -> ids.forEach(id -> wordToAntonymsToBeCoupled.add(new IdTuple(part.getId(), id))));
        });
        return wordToAntonymsToBeCoupled;
    }

    // TODO: 28/02/23 first 3 calls should be tested
    private void coupleSynonyms(Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets,
                                Map<String, List<Long>> newSynonymsPartIdsWithWord) {
        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        // 11a couple existed SETs among
        // a-x a-y a-z, b = same, c = same
        Set<IdTuple> existedSynonymsToBeCoupled =
                synonymService.crossSetsToCoupleEachMemberOfSet(partToExistedSynonymsNotDublicatingSets);
        // 11b couple all existed with all new
        // each abcxyz with each new SYNs
        Set<IdTuple> existedSynonymsWithNewToBeCoupled =
                synonymService.getExistedSynonymsToNewToBeCoupled(
                        flatMapNotDuplicatingSetsToOneSet(partToExistedSynonymsNotDublicatingSets), newSynonymsPartIdsWithWord);
        // 11c couple all new among each other
        // simply all NEW SYNs among each other
        List<IdTuple> newSynonymsToBeCoupled =
                synonymService.getNewSynonymsToBeCoupled(newSynonymsPartIdsWithWord);

        // 100 LAST step - simply couple IDs in MtoM table
        synonymService.coupleSynonymsIds(existedSynonymsToBeCoupled);
        synonymService.coupleSynonymsIds(existedSynonymsWithNewToBeCoupled);
        synonymService.coupleSynonymsIds(newSynonymsToBeCoupled);
    }

    // TODO: 26/02/23 rephrase - remove all CLEAN FUNCTION to UTIL SERVICE
    // 8 add NEW WORD itself to PARTs IDs to be created
    public Map<String, List<Long>> addNewWordPartsToNewSynonymsPartIds(Word word, Map<String, List<Long>> newSynonymsPartIds) {

        Map<String, List<Long>> newSynonymsPartIdsWithWord = new HashMap<>(newSynonymsPartIds);

        word.getParts().forEach(part -> {
            String partName = part.getName();

            // TODO: 27/02/23 !!! можно идти наоборот от синонимов - и тогда не нужно проверять - ведь у сова точно существует PART (а вот наоборот - как сейчас возможно - поэтому проверяю)
            // TODO: 28/02/23 неверно! со стороны слова - NEW PART слова либо добавляется в синонимы либо само создает одиночный список их синонимов
            // TODO: 28/02/23 но это странный метод - лучше перепроверить

            List<Long> newIds = newSynonymsPartIdsWithWord.get(partName);
            if (newIds == null) {
                ArrayList<Long> ids = new ArrayList<>();
                ids.add(part.getId());
                newSynonymsPartIdsWithWord.put(partName, ids);
            } else {
                newIds.add(part.getId());
            }
        });
        return newSynonymsPartIdsWithWord;
    }

    // 10 FLATMAP of separated SETs to one SET
    // OUTPUT part -> <abcxyz>
    private Map<String, List<Long>> flatMapNotDuplicatingSetsToOneSet(Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets) {
        return partToExistedSynonymsNotDublicatingSets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().flatMap(Collection::stream).toList()));
    }

    // 2 SYNs to COLLECTION
    private Set<String> flatMapAllSynonymsToOneSet(Map<String, Set<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

}
