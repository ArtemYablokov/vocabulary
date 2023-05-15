package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.PartAndWordRus;
import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class FirstLevelService {

    private final WordDao wordDao;
    private final SynonymUtilService synonymUtilService;
    private final SecondLevelService secondLevelService;

    public FirstLevelService(WordDao wordDao, SynonymUtilService synonymUtilService, SecondLevelService secondLevelService) {
        this.wordDao = wordDao;
        this.synonymUtilService = synonymUtilService;
        this.secondLevelService = secondLevelService;
    }


    public <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(
            Map<String, Collection<T>> existedSynonyms, Map<String, Collection<T>> existedAntonyms) {

        Map<String, Collection<Set<Long>>> setsOfExistedSynonyms = getSetsOfCoupledSynonyms(existedSynonyms);
        Map<String, Collection<Set<Long>>> setsOfExistedAntonymsOfAntonyms = getSetsOfCoupledAntonyms(existedAntonyms);
        Map<String, Collection<Set<Long>>> existedSynonymsToBeCoupledAsSynonyms = synonymUtilService.merge2maps(setsOfExistedSynonyms, setsOfExistedAntonymsOfAntonyms);

        return synonymUtilService.filterToUniqueSets(existedSynonymsToBeCoupledAsSynonyms);
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

    private <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getSetsOfCoupledSynonyms(Map<String, Collection<T>> existedSynonyms) {

        return existedSynonyms.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            Collection<T> listOfParts = entry.getValue();
                            Collection<Set<Long>> synonymsId = new ArrayList<>();

                            listOfParts.forEach(part -> {
                                Set<Long> synonymPartsId = new HashSet<>();
                                synonymPartsId.add(part.getId()); // INCLUDING ITSELF
                                synonymPartsId.addAll(part.getSynonyms().stream().map(PartAndWordRus::getId).collect(Collectors.toSet()));
                                synonymsId.add(synonymPartsId);
                            });
                            return synonymsId;
                        }));
    }

    // TODO: 06/03/23 UTIL
    public Map<String, List<Long>> getNewSyn_AntPartIds(Map<String, Collection<String>> basicPartToSyn_AntMap, Set<Word> existedSyn_AntFromRepo) {

        List<Word> wordsToBeCreated = secondLevelService.getWordsToBeCreated(basicPartToSyn_AntMap, existedSyn_AntFromRepo);
        List<Word> wordsToBeUpdatedWithNewParts = secondLevelService.getWordsToBeUpdatedWithNewParts(basicPartToSyn_AntMap, existedSyn_AntFromRepo);

        List<Word> savedWords = wordDao.saveAllNewWords(wordsToBeCreated);
        List<Word> updatedWords = wordDao.updateAllWords(wordsToBeUpdatedWithNewParts);

        return secondLevelService.getPartIdsFromSavedWords(basicPartToSyn_AntMap, Stream.concat(savedWords.stream(), updatedWords.stream()).toList());
    }

    public Map<String, Collection<Part>> getExistedParts(Map<String, Collection<String>> basicPartToSyn_AntMap, Set<Word> wordsFromRepo) {
        Map<String, Collection<Part>> partToExistedSynOrAntIds = new HashMap<>();

        basicPartToSyn_AntMap.forEach((partOfSpeech, synOrArts) -> {
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


    private <T extends PartAndWordRus> Map<String, Collection<Set<Long>>> getSetsOfCoupledAntonyms(Map<String, Collection<T>> existedAntonyms) {
        return existedAntonyms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {

            Collection<T> listOfParts = entry.getValue();
            Collection<Set<Long>> synonymsId = new ArrayList<>();
            listOfParts.forEach(part -> {
                List<? extends PartAndWordRus> antonyms = part.getAntonyms();
                if (!CollectionUtils.isEmpty(antonyms)) { // purpose - NOT TO ADD EMPTY SET
                    synonymsId.add(antonyms.stream().map(PartAndWordRus::getId).collect(Collectors.toSet())); // EXCLUDING ITSELF
                }
            });

            return synonymsId;
        }));
    }

    public List<IdTuple> coupleSynonymsAndAntonymsAsAntonyms(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets,
                                                             Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets,
                                                             Map<String, List<Long>> newSynonymsWithWord,
                                                             Map<String, List<Long>> newAntonyms,
                                                             DataExtractorFunctionName functionName) {

        List<IdTuple> idTuples = secondLevelService.coupleExistedAntAndSynAsAnt(existedSynonymsUniqueSets, existedAntonymsUniqueSets, functionName);

        // TODO: 06/03/23 service get as arg it's own output - not cool
        List<IdTuple> tuples1 = synonymUtilService.crossCouple2Lists(
                synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedSynonymsUniqueSets), newAntonyms);

        List<IdTuple> tuples2 = synonymUtilService.crossCouple2Lists(newSynonymsWithWord,
                synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedAntonymsUniqueSets));

        List<IdTuple> tuples = synonymUtilService.crossCouple2Lists(newSynonymsWithWord, newAntonyms);

        return Stream.of(idTuples, tuples1, tuples2, tuples).flatMap(Collection::stream).toList();
    }

    public List<IdTuple> coupleExistedAndNewAsSynonyms(Map<String, Collection<Set<Long>>> uniqueSynonymsSets,
                                                       Map<String, List<Long>> newSynonymsPartIdsWithWord) {
        // 11a couple existed SETs among
        // a-x a-y a-z, b = same, c = same
        Set<IdTuple> existedSynonymsToBeCoupled =
                synonymUtilService.crossCoupleExistedSetsAsSyn(uniqueSynonymsSets);
        // 11b couple all existed with all new
        // each abcxyz with each new SYNs
        List<IdTuple> existedSynonymsWithNewToBeCoupled =
                synonymUtilService.crossCouple2Lists(
                        synonymUtilService.flatMapNotDuplicatingSetsToOneSet(uniqueSynonymsSets), newSynonymsPartIdsWithWord);
        // 11c couple all new among each other
        // simply all NEW SYNs among each other
        List<IdTuple> newSynonymsToBeCoupled =
                synonymUtilService.crossCoupleMembersOfList(newSynonymsPartIdsWithWord);

        return Stream.of(existedSynonymsToBeCoupled, existedSynonymsWithNewToBeCoupled, newSynonymsToBeCoupled).flatMap(Collection::stream).toList();
    }


}
