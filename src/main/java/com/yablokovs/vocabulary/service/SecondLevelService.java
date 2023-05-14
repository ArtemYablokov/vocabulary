package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
public class SecondLevelService {

    private final DAOService daoService;
    private final SynonymUtilService synonymUtilService;

    public SecondLevelService(DAOService daoService, SynonymUtilService synonymUtilService) {
        this.daoService = daoService;
        this.synonymUtilService = synonymUtilService;
    }


    // TODO: 05/03/23 REFACTOR - метод гавно (NPE)
    public List<IdTuple> coupleExistedAntAndSynAsAnt(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets,
                                                     Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets,
                                                     DataExtractorFunctionName functionName) {
        List<IdTuple> idTuples = new ArrayList<>();

        Function<Long, Set<Long>> idsExtractor;
        if (functionName == DataExtractorFunctionName.GET_ENG_ANTONYM_SET_BY_SYNONYM) {
            idsExtractor = daoService::findEngAntonymsByPartId;
        } else if (functionName == DataExtractorFunctionName.GET_RUS_ANTONYM_SET_BY_SYNONYM) {
            idsExtractor = daoService::findRusAntonymsByRusWordId;
        } else if (functionName == DataExtractorFunctionName.FIND_ENG_SYNONYMS_BY_RUS_WORD_ID) {
            idsExtractor = daoService::findEngSynonymsByRusWordId;
        } else if (functionName == DataExtractorFunctionName.FIND_ENG_ANTONYMS_BY_RUS_WORD_ID) {
            idsExtractor = daoService::findEngAntonymsByRusWordId;
        } else {
            throw new RuntimeException("passed wrong data retrieve function name");
        }

        existedSynonymsUniqueSets.forEach((part, listOfSynSets) -> {
            Collection<Set<Long>> listOfAntSets = existedAntonymsUniqueSets.get(part);
            if (!CollectionUtils.isEmpty(listOfAntSets)) {

                listOfSynSets.forEach(synSet -> {
                    Long anySynonym = synSet.iterator().next();
                    Set<Long> foundAntonymsBySynonym = idsExtractor.apply(anySynonym);

                    if (!CollectionUtils.isEmpty(foundAntonymsBySynonym)) {
                        Long anyFoundAntonym = foundAntonymsBySynonym.iterator().next();

                        listOfAntSets.forEach(antSet -> {
                            if (!antSet.contains(anyFoundAntonym)) {
                                idTuples.addAll(synonymUtilService.crossCouple2Sets(synSet, antSet));
                            }
                        });
                    } else {
                        listOfAntSets.forEach(antSet -> idTuples.addAll(synonymUtilService.crossCouple2Sets(synSet, antSet)));
                    }
                });
            }
        });
        return idTuples;
    }

    public List<Word> getWordsToBeUpdatedWithNewParts(Map<String, Collection<String>> partToSYNmap, Set<Word> wordsFromRepo) {
        List<Word> wordsToBeUpdatedWithNewPart = new ArrayList<>();

        partToSYNmap.forEach((partOfSpeech, synOrArts) -> {

            synOrArts.forEach(synOrArt -> {
                wordsFromRepo
                        .stream()
                        // проверяем наличие слов в которых нет этой части речи
                        .filter(w1 -> w1.getName().equals(synOrArt) && w1.getParts().stream().noneMatch(part -> part.getName().equals(partOfSpeech)))
                        .findAny()
                        .ifPresent(w -> {
                            w.addPart(new Part(partOfSpeech));
                            wordsToBeUpdatedWithNewPart.add(w);
                        });
            });
        });

        return wordsToBeUpdatedWithNewPart;
    }

    public List<Word> getWordsToBeCreated(Map<String, Collection<String>> basicPartToSYNANTmap, Set<Word> wordsFromRepo) {

        Map<String, Word> wordsToBeSaved = new HashMap<>();

        basicPartToSYNANTmap.forEach((partOfSpeech, synOrAnts) -> {

            synOrAnts.forEach(synOrAnt -> {
                boolean noneMatch = wordsFromRepo.stream().noneMatch(word -> word.getName().equals(synOrAnt));
                if (noneMatch) {
                    Word word = wordsToBeSaved.get(synOrAnt);
                    if (word != null) { // check if for different parts two equal words
                        word.addPart(new Part(partOfSpeech));
                    } else {
                        Word newWord = new Word(synOrAnt);
                        newWord.addPart(new Part(partOfSpeech));
                        wordsToBeSaved.put(synOrAnt, newWord);
                    }
                }
            });
        });

        return wordsToBeSaved.values().stream().toList();
    }

    public Map<String, List<Long>> getPartIdsFromSavedWords(Map<String, Collection<String>> basicPartToSYNmap, List<Word> savedNewParts) {
        Map<String, List<Long>> newSynonymsPartIds = new HashMap<>();

        basicPartToSYNmap.forEach((part, syn) -> {
            List<Long> partIds = new ArrayList<>();

            syn.forEach(s ->
                    savedNewParts.stream().filter(word -> word.getName().equals(s)).findAny().ifPresent(
                            word -> word.getParts()
                                    .stream()
                                    .filter(p -> p.getName().equals(part))
                                    .findAny()
                                    .ifPresent(p -> partIds.add(p.getId()))));

            newSynonymsPartIds.put(part, partIds);
        });

        return newSynonymsPartIds;
    }

}
