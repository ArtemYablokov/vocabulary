package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PartRepository;
import com.yablokovs.vocabulary.repo.SynonymsRepo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    // TODO: 26/02/23 в теории все связанные синонимы от добавленных можно вытащить из результата запроса в БД
    //  (чтобы он возвращал не только первый уровень от WORD - который равен как раз PART, но еще и SYNs от этих PART)

    // input в одной части речи a(bc), b(ca), x(yz)
    // output <abc>, <xyz>
    public Map<String, List<Set<Long>>> filterExistedSynonymsToUniqueSets(Set<String> basicParts,
                                                                          Map<String, List<Long>> partToExistedSYNids,
                                                                          DatabaseName databaseName) {
        Map<String, List<Set<Long>>> posToSetsOfUncoupledUniqueSynonyms = new HashMap<>();

        basicParts.forEach(part -> {
            // смысл SET в том что их между собой нужно будет связать, при этом не связывая одинаковые МНОЖЕСТВА
            // каждый SET представляет собой добавленный SYN + все его SYNs из БД
            List<Set<Long>> setsOfUncoupledSynonyms = new ArrayList<>();

            List<Long> existedSynonymsByPart = partToExistedSYNids.get(part); // a, b, x

            existedSynonymsByPart.forEach(existedSynonymId -> { // a // b // x

                if (!idExistsInList(setsOfUncoupledSynonyms, existedSynonymId)) { // b is skipped

                    // этот запрос вынимает все связанные ID
                    Set<Long> synsBySyn = synonymsRepo.findSynonymsByPartId(existedSynonymId, databaseName); // bc // skip // yz
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

    Map<String, Set<String>> getAllSynOrAntStringSortedByPartOfSpeech(WordFrontEnd wordFrontEnd, Function<PartDto, List<StringHolder>> synonymsOrAntonymsRetriever) {
        Map<String, Set<String>> partOfSpeechToSynonym = new HashMap<>();

//        Map<String, Set<String>> collect = wordFrontEnd.getParts()
//                .stream()
//                .collect(Collectors.toMap((PartDto::getName), partDto -> synonymsOrAntonymsRetriever.apply(partDto).stream().map(StringHolder::getName).collect(Collectors.toSet())));
//
//        collect.forEach((key, value) -> {
//            if (CollectionUtils.isEmpty(value)) {
//                collect.remove(key);
//            }
//        });
//
//        return collect;

        /// TODO: 03.11.2022 word can't be saved without part of speech - need validation on ingoing request - PART NAME
        // TODO: 26/02/23 BLANK checking ???
        wordFrontEnd.getParts()
                .forEach(partDto -> {
                    List<StringHolder> synOrAnts = synonymsOrAntonymsRetriever.apply(partDto);
                    if (!ObjectUtils.isEmpty(synOrAnts)) {

                        Set<String> notBlankSynonymsStrings = synOrAnts.stream()
                                .map(StringHolder::getName)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toSet());

                        if (!ObjectUtils.isEmpty(notBlankSynonymsStrings)) {
                            partOfSpeechToSynonym.put(partDto.getName(), notBlankSynonymsStrings);
                        }
                    }
                });
        return partOfSpeechToSynonym;
    }

    public Set<IdTuple> crossSetsToCoupleEachMemberOfSet(Map<String, List<Set<Long>>> synonymsToBeCoupled) {

        Map<String, List<Set<Long>>> copyOfSynonymsToBeCoupled = Map.copyOf(synonymsToBeCoupled);

        // TODO: 03.11.2022 concurrent COLLECTIONS can be used w/out iterator for removing
        Set<IdTuple> idTuples = new HashSet<>();
        copyOfSynonymsToBeCoupled.forEach((part, listOfSets) -> {
            Iterator<Set<Long>> iterator = listOfSets.iterator();

            while (iterator.hasNext()) {
                Set<Long> next = iterator.next();
                iterator.remove();

                next.forEach(headId ->
                        listOfSets.forEach(childIdSet ->
                                childIdSet.forEach(childId ->
                                        idTuples.add(new IdTuple(headId, childId)))));
            }
        });
        return idTuples;
    }

    // TODO: 01.11.2022 после связывания всех существующих синонимов - каждый из них имеет одинаковый набор -> можно добавлять новое только к одному из
    public Set<IdTuple> getExistedSynonymsToNewToBeCoupled(Map<String, List<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> newSynOrAntPartIds) {
        Set<IdTuple> idTuples = new HashSet<>();

        partToExistedSynonymsToBeCoupledWithNew.forEach((part, existedSynsIds) -> {
            List<Long> newSynsIds = newSynOrAntPartIds.get(part);

            if (newSynsIds != null) {
                existedSynsIds.forEach(existedId -> {
                    newSynsIds.forEach(newId -> idTuples.add(new IdTuple(existedId, newId)));
                });
            }
        });
        return idTuples;
    }

    public List<IdTuple> getNewSynonymsToBeCoupled(Map<String, List<Long>> newSynOrAntPartIds) {
        List<IdTuple> idTuples = new ArrayList<>();

        newSynOrAntPartIds.forEach((part, list) -> {
            for (int i = 0; i < list.size(); i++) {
                Long parentId = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    idTuples.add(new IdTuple(parentId, list.get(j)));
                }
            }
        });
        return idTuples;
    }

    public void coupleSynonymsIds(Collection<IdTuple> idTuples) {
        if (idTuples.isEmpty()) return;
        // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
        idTuples.forEach(idTuple -> synonymsRepo.createReference(idTuple.getChild(), idTuple.getParent(), DatabaseName.SYNONYM));
    }

    public void coupleAntonymsIds(Collection<IdTuple> idTuples) {
        if (idTuples.isEmpty()) return;
        // TODO: 20.11.2022 refactor to INSERT MULTIPLE RAWS in a time
        idTuples.forEach(idTuple -> synonymsRepo.createReference(idTuple.getChild(), idTuple.getParent(), DatabaseName.ANTONYM));
    }

    public Map<String, List<Long>> getExistedSynonymsIds(Map<String, Set<String>> partToSYNmap, Set<Word> wordsFromRepo) {
        Map<String, List<Long>> partToExistedSynOrAntIds = new HashMap<>();

        partToSYNmap.forEach((partOfSpeech, synOrArts) -> {
            List<Long> existedSynOrAntIds = new ArrayList<>();

            synOrArts.forEach(synOrAnt -> {
                Optional<Word> wordOptional = wordsFromRepo.stream().filter(w -> w.getName().equals(synOrAnt)).findAny();
                if (wordOptional.isPresent()) {
                    Optional<Part> partOptional = wordOptional.get().getParts().stream().filter(part -> part.getName().equals(partOfSpeech)).findAny();
                    partOptional.ifPresent(part -> existedSynOrAntIds.add(part.getId()));
                }
            });
            partToExistedSynOrAntIds.put(partOfSpeech, existedSynOrAntIds);
        });

        return partToExistedSynOrAntIds;
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
    public List<Word> getWordsToBeUpdatedWithNewParts(Map<String, Set<String>> partToSYNmap, Set<Word> wordsFromRepo) {
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

    // TODO: 26/02/23 sorted by PARTs - not used because when the words is saving - 2 same words in different PARTs will be saved twice
    //  (or requires SET to merge 2 PARTS in 1 WORD)
    public Map<String, Set<Word>> getPartToWordsToBeCreated(Map<String, Set<String>> partToSYNmap, Set<Word> existedWordsFromRepo) {
        Map<String, Word> nameToExistedWordMap = existedWordsFromRepo
                .stream()
                .collect(Collectors
                        .toMap(Word::getName, Function.identity()));

        Map<String, Word> nameToWordForCheckingRepeatedBetweenParts = new HashMap<>();

        Map<String, Set<Word>> partToWords = new HashMap<>();


        partToSYNmap.forEach((partOfSpeech, synOrAnts) -> {
            Set<Word> words = new HashSet<>();

            synOrAnts.stream()
                    .filter(syn -> !nameToExistedWordMap.containsKey(syn))
                    .forEach(synOrAnt -> {
                        Word word = nameToWordForCheckingRepeatedBetweenParts.get(synOrAnt);
                        if (word != null) { // check if for different parts two equal words
                            word.addPart(new Part(partOfSpeech));
                            words.add(word);
                        } else {
                            Word newWord = new Word(synOrAnt);
                            newWord.addPart(new Part(partOfSpeech));

                            nameToWordForCheckingRepeatedBetweenParts.put(synOrAnt, newWord);
                            words.add(newWord);
                        }
                    });
            partToWords.put(partOfSpeech, words);
        });

        return partToWords;
    }
    // TODO: 16.11.2022 save all words and return THEM. IDS of new parts will be retrieved later

    public List<Word> mergeNewWordsToBeSaved(List<Word> wordsToBeAddedWithNewParts, List<Word> wordsToBeCreated) {
        List<Word> allWordsToSave = new ArrayList<>();

        allWordsToSave.addAll(wordsToBeAddedWithNewParts);
        allWordsToSave.addAll(wordsToBeCreated);

        return allWordsToSave;
    }

    public Map<String, List<Long>> getNewPartIdsFromSavedWords(Map<String, Set<String>> basicPartToSYNmap, List<Word> savedNewParts) {
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
