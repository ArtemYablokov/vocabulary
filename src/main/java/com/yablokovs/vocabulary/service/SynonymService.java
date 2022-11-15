package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.front.SynonymAntonymHolder;
import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PartRepository;
import com.yablokovs.vocabulary.repo.SynonymsRepo;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SynonymService {

    private final WordRepository wordRepository;
    private final SynonymsRepo synonymsRepo;
    private final PartRepository partRepository;

    public SynonymService(WordRepository wordRepository, SynonymsRepo synonymsRepo, PartRepository partRepository) {
        this.wordRepository = wordRepository;
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

                    existedSynonymsByPart.forEach(
                            existedSynonymId -> {
                                if (!idExistsInList(setsOfUncoupledSynonyms, existedSynonymId)) {
                                    Set<Long> synonymsOfSynonyms = synonymsRepo.findSynonymsByPartId(existedSynonymId);
                                    synonymsOfSynonyms.add(existedSynonymId);
                                    setsOfUncoupledSynonyms.add(synonymsOfSynonyms);
                                }
                            }
                    );
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
    void prepareExistedAndNewPartOfSpeechIds(Map<String, List<String>> partOfSpeechToSynonym,
                                             Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId,
                                             Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId) {
        partOfSpeechToSynonym.forEach(
                (partOfSpeech, listSyn) -> {
                    ArrayList<Long> newSynonyms = new ArrayList<>();
                    ArrayList<Long> existedSynonyms = new ArrayList<>();
                    listSyn.forEach(syn -> {
                        // TODO: 31.10.2022 проверить что для второго прохода (другой части речи) если слова совпадают - не создастся новое слово!
                        Optional<Word> synonymByName = wordRepository.findByName(syn);
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
//        newSynonym.setNumberOfSearches(1L);
        newSynonym.setParts(List.of(new Part(partOfSpeech)));
        // TODO: 04.11.2022 create ParentService that will include Word and Synonym services
        // or remove coupleSynonyms from WORD SERVICE to SynonymApiService
        // so can ude wordService.saveNewWord
//        part.setWord(newSynonym);

        Word save = wordRepository.save(newSynonym);
        Long partId = save.getParts().get(0).getId();
        newSynonyms.add(partId);
    }

    Map<String, List<String>> preparePartToSynonymMap(WordRequest wordRequest) {
        Map<String, List<String>> partOfSpeechToSynonym = new HashMap<>();
        /// TODO: 03.11.2022 word can't be saved without part of speech - need validation on ingoing request
        wordRequest.getParts()
                .forEach(
                        partDto -> {
                            List<SynonymAntonymHolder> synonyms = partDto.getSynonyms();
                            if (!ObjectUtils.isEmpty(synonyms))
                                partOfSpeechToSynonym.put(partDto.getName(),
                                        synonyms.stream().map(SynonymAntonymHolder::getName).collect(Collectors.toList()));
                        }
                );
        return partOfSpeechToSynonym;
    }

    public Set<IdTuple> getPairsOfExistedSynonymsToBeCoupled(Map<String, List<Set<Long>>> synonymsToBeCoupled) {

        Map<String, List<Set<Long>>> copyOfSynonymsToBeCoupled = Map.copyOf(synonymsToBeCoupled);

        // TODO: 03.11.2022 want to implement concurrent COLLECTIONS
        Set<IdTuple> idTuples = new HashSet<>();
        copyOfSynonymsToBeCoupled.forEach(
                (key, sets) -> {
                    Iterator<Set<Long>> iterator = sets.iterator();
                    while (iterator.hasNext()) {
                        Set<Long> next = iterator.next();
                        iterator.remove();
                        next.forEach(headId ->
                                sets.forEach(childIdSet ->
                                        childIdSet.forEach(childId ->
                                                idTuples.add(new IdTuple(headId, childId))))
                        );
                    }

                }
        );
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

        partToExistedSynonymsToBeCoupledWithNew.forEach(
                (part, set) -> {
                    List<Long> longs = partOfSpeechToNewSynonymAsPartId.get(part);
                    if (longs != null) {
                        set.forEach(
                                existedId -> {
                                    longs.forEach(newId -> idTuples.add(new IdTuple(existedId, newId)));
                                }
                        );
                    }
                }
        );

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
}
