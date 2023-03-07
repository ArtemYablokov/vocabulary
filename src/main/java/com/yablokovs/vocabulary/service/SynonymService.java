package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

// TODO: 15.11.2022 make it private BEAN visibility
@Service
public class SynonymService {

    private final SynonymUtilService synonymUtilService;

    private final SynonymDAOService synonymDAOService;

    public SynonymService(SynonymUtilService synonymUtilService, SynonymDAOService synonymDAOService) {
        this.synonymUtilService = synonymUtilService;
        this.synonymDAOService = synonymDAOService;
    }

    public Map<String, Collection<String>> getBasicMapOfPartToSynOrAnt(WordFrontEnd wordFrontEnd,
                                                                       Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever) {
        Map<String, Collection<String>> partOfSpeechToSynonym = new HashMap<>();

        wordFrontEnd.getParts()
                .forEach(partDto -> {
                    List<StringHolder> synOrAnts = synonymsOrAntonymsRetriever.apply(partDto);
                    if (!ObjectUtils.isEmpty(synOrAnts)) {

                        List<String> notBlankSynonymsStrings = synOrAnts.stream()
                                .map(StringHolder::getName)
                                .filter(StringUtils::isNotBlank)
                                .distinct()
                                .toList();

                        if (!ObjectUtils.isEmpty(notBlankSynonymsStrings)) {
                            partOfSpeechToSynonym.put(partDto.getName(), notBlankSynonymsStrings);
                        }
                    }
                });
        return partOfSpeechToSynonym;
    }

    private boolean idExistsInList(List<Set<Long>> list, Long id) {
        return list.stream().anyMatch(set -> set.contains(id));
    }

    public void coupleSynonymsIds(Collection<IdTuple> idTuples) {
        if (idTuples.isEmpty()) return;
        synonymDAOService.saveSynIdTuple(idTuples);
    }

    public void coupleAntonymsIds(Collection<IdTuple> idTuples) {
        if (idTuples.isEmpty()) return;
        synonymDAOService.saveAntIdTuple(idTuples);
    }

    public Map<String, Collection<Part>> getExistedParts(Map<String, Collection<String>> basicPartToSynOrAntMap, Set<Word> wordsFromRepo) {
        Map<String, Collection<Part>> partToExistedSynOrAntIds = new HashMap<>();

        basicPartToSynOrAntMap.forEach((partOfSpeech, synOrArts) -> {
            List<Part> existedSynOrAntIds = new ArrayList<>();

            synOrArts.forEach(synOrAnt -> {
                Optional<Word> wordOptional = wordsFromRepo.stream().filter(w -> w.getName().equals(synOrAnt)).findAny();
                if (wordOptional.isPresent()) {
                    Word word = wordOptional.get();
                    Optional<Part> partOptional = word.getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findAny();
                    partOptional.ifPresent(existedSynOrAntIds::add);
                }
            });
            partToExistedSynOrAntIds.put(partOfSpeech, existedSynOrAntIds);
        });

        return partToExistedSynOrAntIds;
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

    public List<IdTuple> coupleSynonymsToIdTuples(Map<String, Collection<Set<Long>>> partToExistedSynonymsNotDublicatingSets,
                                                  Map<String, List<Long>> newSynonymsPartIdsWithWord) {
        // 11a couple existed SETs among
        // a-x a-y a-z, b = same, c = same
        Set<IdTuple> existedSynonymsToBeCoupled =
                synonymUtilService.crossCoupleInternalExistedSetsAsSyn(partToExistedSynonymsNotDublicatingSets);
        // 11b couple all existed with all new
        // each abcxyz with each new SYNs
        List<IdTuple> existedSynonymsWithNewToBeCoupled =
                synonymUtilService.crossCouple2Lists(
                        synonymUtilService.flatMapNotDuplicatingSetsToOneSet(partToExistedSynonymsNotDublicatingSets), newSynonymsPartIdsWithWord);
        // 11c couple all new among each other
        // simply all NEW SYNs among each other
        List<IdTuple> newSynonymsToBeCoupled =
                synonymUtilService.crossCoupleInternallyNewAsSyn(newSynonymsPartIdsWithWord);

        List<IdTuple> idTuplesResult = new ArrayList<>();
        idTuplesResult.addAll(existedSynonymsToBeCoupled);
        idTuplesResult.addAll(existedSynonymsWithNewToBeCoupled);
        idTuplesResult.addAll(newSynonymsToBeCoupled);

        return idTuplesResult;
    }

    public List<IdTuple> coupleAntonymsToIdTuples(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets,
                                                  Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets,
                                                  Map<String, List<Long>> newSynonymsWithWord,
                                                  Map<String, List<Long>> newAntonyms) {

        List<IdTuple> idTuples = synonymUtilService.coupleExistedAntAndSynAsAnt(existedSynonymsUniqueSets, existedAntonymsUniqueSets);
        // TODO: 06/03/23 service get as arg it's own output - not cool
        List<IdTuple> tuples1 = synonymUtilService.crossCouple2Lists(
                synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedSynonymsUniqueSets), newAntonyms);

        List<IdTuple> tuples2 = synonymUtilService.crossCouple2Lists(
                synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedAntonymsUniqueSets), newSynonymsWithWord);

        List<IdTuple> tuples = synonymUtilService.crossCouple2Lists(newSynonymsWithWord, newAntonyms);

        return Stream.of(idTuples, tuples1, tuples2, tuples).flatMap(Collection::stream).toList();
    }
}
