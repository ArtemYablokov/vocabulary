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
import java.util.stream.Collectors;

// TODO: 15.11.2022 make it private BEAN visibility
@Service
public class SynonymService {

    private final SynonymUtilService synonymUtilService;

    private final SynonymDAOService synonymDAOService;

    public SynonymService(SynonymUtilService synonymUtilService, SynonymDAOService synonymDAOService) {
        this.synonymUtilService = synonymUtilService;
        this.synonymDAOService = synonymDAOService;
    }

    public Map<String, Collection<String>> getBasicMapOfPartToSynOrAnt(WordFrontEnd wordFrontEnd, Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever) {
        Map<String, Collection<String>> partOfSpeechToSynonym = new HashMap<>();

        /// TODO: 03.11.2022 word can't be saved without part of speech - need validation on ingoing request - PART NAME
        // TODO: 26/02/23 BLANK checking ???
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

    @Deprecated
    // TODO: 26/02/23 в теории все связанные синонимы от добавленных можно вытащить из результата запроса в БД
    //  (чтобы он возвращал не только первый уровень от WORD - который равен как раз PART, но еще и SYNs от этих PART)
    // TODO: 02/03/23 зачем здесь basicParts ?
    // input в одной части речи a(bc), b(ca), x(yz)
    // output <abc>, <xyz>
    public Map<String, Collection<Set<Long>>> filterExistedSynonymsToUniqueSets(Set<String> basicParts,
                                                                          Map<String, List<Long>> partToExistedSYNids) {
        Map<String, Collection<Set<Long>>> posToSetsOfUncoupledUniqueSynonyms = new HashMap<>();

        basicParts.forEach(part -> {
            List<Set<Long>> setsOfUncoupledSynonyms = new ArrayList<>();

            List<Long> existedSynonymsByPart = partToExistedSYNids.get(part); // a, b, x

            // TODO: 03/03/23 место использования связанных ID + его синонимы (+ антонимы его антонимов так же сюда надо передавать)
            existedSynonymsByPart.forEach(existedSynonymId -> { // a // b // x

                if (!idExistsInList(setsOfUncoupledSynonyms, existedSynonymId)) { // b is skipped

                    // TODO: 02/03/23 тогда здесь не прийдется ходить за синонимами по ID
                    // этот запрос вынимает все связанные ID
                    Set<Long> synsBySyn = synonymDAOService.findSynonymsByPartId(existedSynonymId); // bc // skip // yz
                    synsBySyn.add(existedSynonymId); // adding itself -> abc // skip // -> xyz
                    setsOfUncoupledSynonyms.add(synsBySyn); // <abc> // skip // <xyz>
                }
            });
            posToSetsOfUncoupledUniqueSynonyms.put(part, setsOfUncoupledSynonyms);
        });
        return posToSetsOfUncoupledUniqueSynonyms;
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

    @Deprecated
    // TODO: 03/03/23 в принципе можно возвращать сразу объединенные множества - сам ID и его синонимы / антонимы
    // TODO: 02/03/23 СТОП !!! а почему здесь сразу не возвращать извлеченные сининмы ??? их ID ???
    // тогда это превратится в 2 одинаковых метода - один для SYN/ANT
    public Map<String, List<Long>> getExistedSynonymsIds(Map<String, Collection<String>> basicPartToSynOrAntMap, Set<Word> wordsFromRepo) {
        Map<String, List<Long>> partToExistedSynOrAntIds = new HashMap<>();

        basicPartToSynOrAntMap.forEach((partOfSpeech, synOrArts) -> {
            List<Long> existedSynOrAntIds = new ArrayList<>();

            synOrArts.forEach(synOrAnt -> {
                Optional<Word> wordOptional = wordsFromRepo.stream().filter(w -> w.getName().equals(synOrAnt)).findAny();
                if (wordOptional.isPresent()) {
                    Word word = wordOptional.get();
                    Optional<Part> partOptional = word.getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findAny();
                    partOptional.ifPresent(part -> {
                        existedSynOrAntIds.add(part.getId());
                    });
                }
            });
            partToExistedSynOrAntIds.put(partOfSpeech, existedSynOrAntIds);
        });

        return partToExistedSynOrAntIds;
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

    // TODO: 16.11.2022 rewrite method to return Words to save BEFORE it check that: 1 adding to existing Word works (by ID) YES. 2 adding to collection execute One SQL NO 3 ids added to Parts when saving Word YES
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

    // TODO: 17.11.2022  synonyms.removeAll(existedWords.stream().map(Word::getName).collect(Collectors.toSet())); // O(n)
    // need to check case with same words in different parts
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

    public List<IdTuple> coupleSynonyms(Map<String, Collection<Set<Long>>> partToExistedSynonymsNotDublicatingSets,
                                        Map<String, List<Long>> newSynonymsPartIdsWithWord) {
        // 11a couple existed SETs among
        // a-x a-y a-z, b = same, c = same
        Set<IdTuple> existedSynonymsToBeCoupled =
                synonymUtilService.crossCoupleExistedSetsInternallyAsSyn(partToExistedSynonymsNotDublicatingSets);
        // 11b couple all existed with all new
        // each abcxyz with each new SYNs
        List<IdTuple> existedSynonymsWithNewToBeCoupled =
                synonymUtilService.crossCouple2Lists(
                        synonymUtilService.flatMapNotDuplicatingSetsToOneSet(partToExistedSynonymsNotDublicatingSets), newSynonymsPartIdsWithWord);
        // 11c couple all new among each other
        // simply all NEW SYNs among each other
        List<IdTuple> newSynonymsToBeCoupled =
                synonymUtilService.crossCoupleNewInternally(newSynonymsPartIdsWithWord);

        List<IdTuple> idTuplesResult = new ArrayList<>();
        idTuplesResult.addAll(existedSynonymsToBeCoupled);
        idTuplesResult.addAll(existedSynonymsWithNewToBeCoupled);
        idTuplesResult.addAll(newSynonymsToBeCoupled);

        return idTuplesResult;
    }


    @Deprecated
    public List<IdTuple> coupleAntonyms(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets,
                                        Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets,
                                        Map<String, List<Long>> newSynonymsWithWord,
                                        Map<String, List<Long>> newAntonyms) {

        // 1 новые и старые SYN пересекать с WORD уже не нужно
        // 2 а вот для антонимов имеет смысл пересечь WORD с ними (как старыми, так и новыми)
        // 3 тогда если положить слово в NEW SYN - оно будет связано и со старыми, и с новыми ANT (а оно уже там и лежит  )

        List<IdTuple> idTuples = synonymUtilService.coupleExistedANTandSYNAsAnt(existedSynonymsUniqueSets, existedAntonymsUniqueSets);

        List<IdTuple> tuples = synonymUtilService.crossCouple2Lists(newSynonymsWithWord, newAntonyms);
        List<IdTuple> tuples1 = synonymUtilService.crossCouple2Lists(synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedSynonymsUniqueSets), newAntonyms);
        List<IdTuple> tuples2 = synonymUtilService.crossCouple2Lists(synonymUtilService.flatMapNotDuplicatingSetsToOneSet(existedAntonymsUniqueSets), newSynonymsWithWord);

        List<IdTuple> idTuplesResult = new ArrayList<>();
        idTuplesResult.addAll(idTuples);
        idTuplesResult.addAll(tuples);
        idTuplesResult.addAll(tuples1);
        idTuplesResult.addAll(tuples2);

        return idTuplesResult;
    }
}
