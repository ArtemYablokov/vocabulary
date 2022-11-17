package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.front.SynonymOrAntonymStringHolder;
import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PartRepository;
import com.yablokovs.vocabulary.repo.SynonymsRepo;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

// TODO: 15.11.2022 make it private BEAN visibility
@Service
public class SynonymService {

    // TODO: 16.11.2022 remove both from here ???
    private final WordService wordService;
    private final PartRepository partRepository;


    private final SynonymsRepo synonymsRepo;


    public SynonymService(WordService wordService, SynonymsRepo synonymsRepo, PartRepository partRepository) {
        this.wordService = wordService;
        this.synonymsRepo = synonymsRepo;
        this.partRepository = partRepository;
    }

    public Map<String, List<Set<Long>>> filterExistedSynonymsToUniqueSets(Set<String> partOfSpeeches,
                                                                          Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId) {
        Map<String, List<Set<Long>>> posToSetsOfUncoupledUniqueSynonyms = new HashMap<>();

        partOfSpeeches.forEach(
                partOfSpeech -> {
                    List<Set<Long>> setsOfUncoupledSynonyms = new ArrayList<>();
                    List<Long> existedSynonymsByPart = partOfSpeechToExistedSynonymAsPartId.get(partOfSpeech);

                    existedSynonymsByPart.forEach(existedSynonymId -> {
                        if (!idExistsInList(setsOfUncoupledSynonyms, existedSynonymId)) {
                            Set<Long> synonymsOfSynonyms = synonymsRepo.findSynonymsByPartId(existedSynonymId);
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
    void prepareExistedAndNewPartOfSpeechIds(Map<String, Set<String>> partOfSpeechToSynonym,
                                             Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId,
                                             Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId) {
        partOfSpeechToSynonym.forEach(
                (partOfSpeech, listSyn) -> {
                    ArrayList<Long> newSynonyms = new ArrayList<>();
                    ArrayList<Long> existedSynonyms = new ArrayList<>();
                    listSyn.forEach(syn -> {
                        // TODO: 31.10.2022 проверить что для второго прохода (другой части речи) если слова совпадают - не создастся новое слово!
                        Optional<Word> synonymByName = /*wordService.findByName(syn)*/ null;
                        if (synonymByName.isPresent()) {
                            Word existedWord = synonymByName.get();
                            // TODO: 31.10.2022 need to fetch Parts with name and ID
                            // TODO: 31.10.2022 implement preventing doubling Pos's
                            Optional<Part> existedPart = existedWord.getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findFirst();
                            if (existedPart.isPresent()) {
                                existedSynonyms.add(existedPart.get().getId());
                            } else {
                                Part part = new Part();
                                part.setName(partOfSpeech);
                                part.setWord(existedWord);
                                partRepository.save(part);
                                newSynonyms.add(part.getId());
                            }
                        } else {
                            createWordWithPartAndSynonym(partOfSpeech, newSynonyms, syn);
                        }
                    });
                    partOfSpeechToNewSynonymAsPartId.put(partOfSpeech, newSynonyms);
                    partOfSpeechToExistedSynonymAsPartId.put(partOfSpeech, existedSynonyms);
                });
    }

    private void createWordWithPartAndSynonym(String partOfSpeech, ArrayList<Long> newSynonyms, String syn) {
        Word newSynonym = new Word(syn);
        // newSynonym.setParts(List.of(new Part(partOfSpeech)));
        newSynonym.addPart(new Part(partOfSpeech));

        // dont need to coupled parents with child, because its coupled with Word::addPart
//        wordService.saveNewWordWithPartsAndDefinitions(newSynonym);
//        part.setWord(newSynonym);

        // TODO: 15.11.2022 what if set 1 by default
        newSynonym.setNumberOfSearches(1L);

        Word save = wordService.save(newSynonym);
        Long partId = save.getParts().iterator().next().getId();

        // TODO: 15.11.2022 method shouldn't modify external data (newSynonyms)
        newSynonyms.add(partId);
    }

    Map<String, Set<String>> preparePartToSynonymMap(WordRequest wordRequest) {
        Map<String, Set<String>> partOfSpeechToSynonym = new HashMap<>();
        /// TODO: 03.11.2022 word can't be saved without part of speech - need validation on ingoing request
        wordRequest.getParts()
                .forEach(partDto -> {
                    List<SynonymOrAntonymStringHolder> synonyms = partDto.getSynonyms();
                    if (!ObjectUtils.isEmpty(synonyms))
                        partOfSpeechToSynonym.put(partDto.getName(),
                                synonyms.stream().map(SynonymOrAntonymStringHolder::getName).collect(Collectors.toSet()));
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

    public void coupleIds(Collection<IdTuple> idTuples) {
        if (idTuples.isEmpty()) return;
        idTuples.forEach(idTuple -> {
            synonymsRepo.createReference(idTuple.getChild(), idTuple.getParent());
        });
    }


    @Deprecated
    void coupleExistedSynonymsWithNew(Map<String, Set<Long>> idsTobeCoupledWithNewSynonyms, Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId) {
        partOfSpeechToNewSynonymAsPartId.forEach((part, listIds) -> {

            Set<Long> longs = idsTobeCoupledWithNewSynonyms.get(part);
            if (longs != null) {
                crossCoupleExistedSynonyms(listIds, longs);
            }
        });
    }

    @Deprecated
    private void crossCoupleExistedSynonyms(Collection<Long> synonymsIds, Collection<Long> synonymsIds1) {
        synonymsIds.forEach(id -> {
            synonymsIds1.forEach(childId -> {
                synonymsRepo.createReference(id, childId);
            });
        });
    }

    public Map<String, List<Long>> getExistedSynonymsIds(Map<String, Set<String>> partOfSpeechToSynonym, Set<Word> allWordsWithPartsBySynonymsStrings) {
        Map<String, List<Long>> posToExistedSynonymsIds = new HashMap<>();

        partOfSpeechToSynonym.forEach((partOfSpeech, synonyms) -> {
            List<Long> existedSynonymsIds = new ArrayList<>();

            synonyms.forEach(synonym -> {
                Optional<Word> wordOptional = allWordsWithPartsBySynonymsStrings.stream()
                        .filter(w -> w.getName().equals(synonym))
                        .findAny();
                if (wordOptional.isPresent()) {
                    Optional<Part> partOptional = wordOptional.get().getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findAny();
                    partOptional.ifPresent(part -> existedSynonymsIds.add(part.getId()));
                }
            });
            posToExistedSynonymsIds.put(partOfSpeech, existedSynonymsIds);
        });

        return posToExistedSynonymsIds;
    }

    // TODO: 16.11.2022 rewrite method to return Words to save BEFORE it check that: 1 adding to existing Word works (by ID). 2 adding to collection execute One SQL 3 ids added to Parts when saving Word
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

    //        synonyms.removeAll(existedWords.stream().map(Word::getName).collect(Collectors.toSet())); // O(n)
    public Map<String, List<Word>> getWordsToBeCreated(Map<String, Set<String>> partOfSpeechToSynonym, Set<Word> allWordsWithPartsBySynonymsStrings) {

        Map<String, List<Word>> wordsToBeCreated = new HashMap<>();

        partOfSpeechToSynonym.forEach((partOfSpeech, synonyms) -> {

            Map<String, Word> wordsToBeSaved = new HashMap<>();
            synonyms.forEach(synonym -> {
                boolean noneMatch = allWordsWithPartsBySynonymsStrings.stream().noneMatch(word -> word.getName().equals(synonym));
                if (noneMatch) {
                    Word word = wordsToBeSaved.get(synonym);
                    if (word != null) {
                        word.addPart(new Part(partOfSpeech));
                    } else {
                        Word newWord = new Word(synonym);
                        newWord.addPart(new Part(partOfSpeech));
                        wordsToBeSaved.put(synonym, newWord);
                    }
                }
            });
            wordsToBeCreated.put(partOfSpeech, wordsToBeSaved.values().stream().toList());
        });

        return wordsToBeCreated;
    }

    public Map<String, List<Long>> getNewSynonymsPartIds(Map<String, List<Long>> wordIdsToBeAddedWithNewParts, Map<String, List<Word>> wordsToBeCreated) {

        // TODO: 16.11.2022 save all words and return IDS of new parts
        return null;
    }
}
