package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.SynonymOrAntonymStringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PartRepository;
import com.yablokovs.vocabulary.repo.SynonymsRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yablokovs.vocabulary.repo.SynonymsRepo.DatabaseName;

// TODO: 15.11.2022 make it private BEAN visibility
@Service
public class SynonymService {

    // TODO: 16.11.2022 remove both from here ???
    private final PartRepository partRepository;

    private final SynonymsRepo synonymsRepo;


    public SynonymService(WordService wordService, SynonymsRepo synonymsRepo, PartRepository partRepository) {
        this.synonymsRepo = synonymsRepo;
        this.partRepository = partRepository;
    }

    public Map<String, List<Set<Long>>> filterExistedSynonymsToUniqueSets(Set<String> partOfSpeeches,
                                                                          Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId,
                                                                          DatabaseName databaseName) {
        Map<String, List<Set<Long>>> posToSetsOfUncoupledUniqueSynonyms = new HashMap<>();

        partOfSpeeches.forEach(
                partOfSpeech -> {
                    List<Set<Long>> setsOfUncoupledSynonyms = new ArrayList<>();
                    List<Long> existedSynonymsByPart = partOfSpeechToExistedSynonymAsPartId.get(partOfSpeech);

                    existedSynonymsByPart.forEach(existedSynonymId -> {
                        if (!idExistsInList(setsOfUncoupledSynonyms, existedSynonymId)) {
                            Set<Long> synonymsOfSynonyms = synonymsRepo.findSynonymsByPartId(existedSynonymId, databaseName);
                            synonymsOfSynonyms.add(existedSynonymId);
                            setsOfUncoupledSynonyms.add(synonymsOfSynonyms);
                        }
                    });
                    posToSetsOfUncoupledUniqueSynonyms.put(partOfSpeech, setsOfUncoupledSynonyms);
                }
        );
        return posToSetsOfUncoupledUniqueSynonyms;
    }

    private boolean idExistsInList(List<Set<Long>> list, Long id) {
        return list.stream().anyMatch(set -> set.contains(id));
    }

    //    @Transactional
    // TODO: 01.11.2022 need to split to 2 methods
//    void prepareExistedAndNewPartOfSpeechIds(Map<String, Set<String>> partOfSpeechToSynonym,
//                                             Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId,
//                                             Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId) {
//        partOfSpeechToSynonym.forEach(
//                (partOfSpeech, listSyn) -> {
//                    ArrayList<Long> newSynonyms = new ArrayList<>();
//                    ArrayList<Long> existedSynonyms = new ArrayList<>();
//                    listSyn.forEach(syn -> {
//                        // TODO: 31.10.2022 проверить что для второго прохода (другой части речи) если слова совпадают - не создастся новое слово!
//                        Optional<Word> synonymByName = /*wordService.findByName(syn)*/ null;
//                        if (synonymByName.isPresent()) {
//                            Word existedWord = synonymByName.get();
//                            // TODO: 31.10.2022 need to fetch Parts with name and ID
//                            // TODO: 31.10.2022 implement preventing doubling Pos's
//                            Optional<Part> existedPart = existedWord.getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findFirst();
//                            if (existedPart.isPresent()) {
//                                existedSynonyms.add(existedPart.get().getId());
//                            } else {
//                                Part part = new Part();
//                                part.setName(partOfSpeech);
//                                part.setWord(existedWord);
//                                partRepository.save(part);
//                                newSynonyms.add(part.getId());
//                            }
//                        } else {
//                            // createWordWithPartAndSynonym(partOfSpeech, newSynonyms, syn);
//                        }
//                    });
//                    partOfSpeechToNewSynonymAsPartId.put(partOfSpeech, newSynonyms);
//                    partOfSpeechToExistedSynonymAsPartId.put(partOfSpeech, existedSynonyms);
//                });
//    }

//    private void createWordWithPartAndSynonym(String partOfSpeech, ArrayList<Long> newSynonyms, String syn) {
//        Word newSynonym = new Word(syn);
//        // newSynonym.setParts(List.of(new Part(partOfSpeech)));
//        newSynonym.addPart(new Part(partOfSpeech));
//
//        // don't need to coupled parents with child, because its coupled with Word::addPart
////        wordService.saveNewWordWithPartsAndDefinitions(newSynonym);
////        part.setWord(newSynonym);
//
//        // TODO: 15.11.2022 what if set 1 by default
//        newSynonym.setNumberOfSearches(1L);
//
//        // Word save = wordService.save(newSynonym);
//        // Long partId = save.getParts().iterator().next().getId();
//
//        // TODO: 15.11.2022 method shouldn't modify external data (newSynonyms)
//        // newSynonyms.add(partId);
//    }

    Map<String, Set<String>> getAllSynOrAntStringSortedByPartOfSpeech(WordFrontEnd wordFrontEnd, Function<PartDto, List<SynonymOrAntonymStringHolder>> synonymsOrAntonymsRetriever) {
        Map<String, Set<String>> partOfSpeechToSynonym = new HashMap<>();
        /// TODO: 03.11.2022 word can't be saved without part of speech - need validation on ingoing request
        wordFrontEnd.getParts()
                .forEach(partDto -> {
                    List<SynonymOrAntonymStringHolder> synonyms = synonymsOrAntonymsRetriever.apply(partDto);
                    if (!ObjectUtils.isEmpty(synonyms)) {

                        Set<String> notBlankSynonymsStrings = synonyms.stream()
                                .map(SynonymOrAntonymStringHolder::getName)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toSet());

                        if (!ObjectUtils.isEmpty(notBlankSynonymsStrings)) {
                            partOfSpeechToSynonym.put(partDto.getName(), notBlankSynonymsStrings);
                        }
                    }
                });
        return partOfSpeechToSynonym;
    }

    public Set<IdTuple> getPairsOfExistedSynonymsToBeCoupled(Map<String, List<Set<Long>>> synonymsToBeCoupled) {

        Map<String, List<Set<Long>>> copyOfSynonymsToBeCoupled = Map.copyOf(synonymsToBeCoupled);

        // TODO: 03.11.2022 want to implement concurrent COLLECTIONS
        Set<IdTuple> idTuples = new HashSet<>();
        copyOfSynonymsToBeCoupled.forEach((key, sets) -> {
            Iterator<Set<Long>> iterator = sets.iterator();
            while (iterator.hasNext()) {
                Set<Long> next = iterator.next();
                iterator.remove();
                next.forEach(headId ->
                        sets.forEach(childIdSet ->
                                childIdSet.forEach(childId ->
                                        idTuples.add(new IdTuple(headId, childId)))));
            }
        });
        return idTuples;
    }

    public Map<String, Set<Long>> getExistedSynonymsToBeCoupledWithNew(Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets) {
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = new HashMap<>();

        partToExistedSynonymsUniqueSets.forEach((part, list) ->
                partToExistedSynonymsToBeCoupledWithNew.put(part, list.stream().flatMap(Collection::stream).collect(Collectors.toSet()))
        );

        return partToExistedSynonymsToBeCoupledWithNew;
    }

    // TODO: 01.11.2022 после связывания всех существующих синонимов - каждый из них имеет одинаковый набор -> можно добавлять новое только к одному из
    public Set<IdTuple> getExistedSynonymsToNewToBeCoupled(Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId) {
        Set<IdTuple> idTuples = new HashSet<>();

        partToExistedSynonymsToBeCoupledWithNew.forEach((part, set) -> {
            List<Long> longs = partOfSpeechToNewSynonymAsPartId.get(part);
            if (longs != null) {
                set.forEach(existedId -> {
                    longs.forEach(newId -> idTuples.add(new IdTuple(existedId, newId)));
                });
            }
        });
        return idTuples;
    }

    public List<IdTuple> getNewSynonymsToBeCoupled(Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId) {
        List<IdTuple> idTuples = new ArrayList<>();

        partOfSpeechToNewSynonymAsPartId.forEach((part, list) -> {
            for (int i = 0; i < list.size(); i++) {
                Long parentId = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    idTuples.add(new IdTuple(parentId, list.get(j)));
                }
            }
        });
        return idTuples;
    }

    public void coupleSynonymsIds(Collection<IdTuple> idTuples, DatabaseName databaseName) {
        if (idTuples.isEmpty()) return;
        // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
        idTuples.forEach(idTuple -> synonymsRepo.createReference(idTuple.getChild(), idTuple.getParent(), databaseName));
    }

    public Map<String, List<Long>> getExistedSynonymsIds(Map<String, Set<String>> partOfSpeechToSynOrAnt, Set<Word> wordsFromRepo) {
        Map<String, List<Long>> posToExistedSynOrAntIds = new HashMap<>();

        partOfSpeechToSynOrAnt.forEach((partOfSpeech, synOrArt) -> {
            List<Long> existedSynOrAntIds = new ArrayList<>();

            synOrArt.forEach(synOrAnt -> {
                Optional<Word> wordOptional = wordsFromRepo.stream()
                        .filter(w -> w.getName().equals(synOrAnt))
                        .findAny();
                if (wordOptional.isPresent()) {
                    Optional<Part> partOptional = wordOptional.get().getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findAny();
                    partOptional.ifPresent(part -> existedSynOrAntIds.add(part.getId()));
                }
            });
            posToExistedSynOrAntIds.put(partOfSpeech, existedSynOrAntIds);
        });

        return posToExistedSynOrAntIds;
    }

    public Map<String, List<Long>> getWordIdsToBeAddedWithNewParts(Map<String, Set<String>> partOfSpeechToSynonym, Set<Word> allWordsWithPartsBySynonymsStrings) {
        Map<String, List<Long>> posToNewSynonymsIds = new HashMap<>();

        partOfSpeechToSynonym.forEach((partOfSpeech, synonyms) -> {
            List<Long> wordsIdsToBeAddedNewPart = new ArrayList<>();

            synonyms.forEach(synonym -> {
                allWordsWithPartsBySynonymsStrings.stream()
                        .filter(w1 -> w1.getName().equals(synonym)
                                && w1.getParts().stream().noneMatch(part -> part.getName().equals(partOfSpeech)))
                        .findAny()
                        .ifPresent(w -> wordsIdsToBeAddedNewPart.add(w.getId()));
            });
            posToNewSynonymsIds.put(partOfSpeech, wordsIdsToBeAddedNewPart);
        });

        return posToNewSynonymsIds;
    }

    // TODO: 16.11.2022 rewrite method to return Words to save BEFORE it check that: 1 adding to existing Word works (by ID) YES. 2 adding to collection execute One SQL NO 3 ids added to Parts when saving Word YES

    public List<Word> getWordsToBeUpdatedWithNewParts(Map<String, Set<String>> partOfSpeechToSynOrAnt, Set<Word> wordsFromRepo) {
        List<Word> wordsToBeUpdatedWithNewPart = new ArrayList<>();

        partOfSpeechToSynOrAnt.forEach((partOfSpeech, synOrArts) -> {

            synOrArts.forEach(synOrArt -> {
                wordsFromRepo.stream()
                        .filter(w1 -> w1.getName().equals(synOrArt)
                                && w1.getParts().stream().noneMatch(part -> part.getName().equals(partOfSpeech)))
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
    public List<Word> getWordsToBeCreated(Map<String, Set<String>> partOfSpeechToSynOrAnt, Set<Word> wordsFromRepo) {

        Map<String, Word> wordsToBeSaved = new HashMap<>();

        partOfSpeechToSynOrAnt.forEach((partOfSpeech, synOrAnts) -> {

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
    // TODO: 16.11.2022 save all words and return THEM. IDS of new parts will be retrieved later

    public List<Word> mergeNewWordsToBeSaved(List<Word> wordsToBeAddedWithNewParts, List<Word> wordsToBeCreated) {
        List<Word> allWordsToSave = new ArrayList<>();

        allWordsToSave.addAll(wordsToBeAddedWithNewParts);
        allWordsToSave.addAll(wordsToBeCreated);

        return allWordsToSave;
    }

    public Map<String, List<Long>> getNewPartIdsFromSavedWords(List<Word> savedWords, Map<String, Set<String>> partOfSpeechToSynOrAnt) {

        Map<String, List<Long>> newSynonymsPartIds = new HashMap<>();

        partOfSpeechToSynOrAnt.forEach((partOfSpeech, synOrAnts) -> {
            List<Long> partIds = new ArrayList<>();

            synOrAnts.forEach(synOrAnt -> {
                Optional<Word> wordOptional = savedWords.stream().filter(w -> w.getName().equals(synOrAnt)).findAny();

                // TODO: 21/02/23 flatMap hard to comprehend
                wordOptional.flatMap(word -> word.getParts().stream()
                        .filter(p -> p.getName().equals(partOfSpeech))
                        .findAny()).ifPresent(p -> partIds.add(p.getId()));
            });
            newSynonymsPartIds.put(partOfSpeech, partIds);
        });
        return newSynonymsPartIds;
    }

    public void addNewWordPartsToNewSynonymsPartIds(Word word, Map<String, List<Long>> newSynonymsPartIds) {
        // у нового слова возьмем части речи
        word.getParts().forEach(
                // попробуем взять для кажждой части речи наши сохраненные синонимы / антонимы
                part -> {
                    String name = part.getName();
                    List<Long> longs = newSynonymsPartIds.get(name);
                    if (longs == null) {
                        ArrayList<Long> longs1 = new ArrayList<>();
                        newSynonymsPartIds.put(name, longs1);
                    } else {
                        longs.add(part.getId());
                    }
                });
    }

//    void coupleExistedSynonymsWithNew(Map<String, Set<Long>> idsTobeCoupledWithNewSynonyms, Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId) {
//        partOfSpeechToNewSynonymAsPartId.forEach((part, listIds) -> {
//
//            Set<Long> longs = idsTobeCoupledWithNewSynonyms.get(part);
//            if (longs != null) {
//                crossCoupleExistedSynonyms(listIds, longs);
//            }
//        });
//    }
//
//    @Deprecated
//    private void crossCoupleExistedSynonyms(Collection<Long> synonymsIds, Collection<Long> synonymsIds1) {
//        synonymsIds.forEach(id -> {
//            synonymsIds1.forEach(childId -> {
//                synonymsRepo.createReference(id, childId, DatabaseName.SYNONYM);
//            });
//        });
//    }
}
