package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PartRepository;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SynonymService {

    private final WordRepository wordRepository;
    private final PartRepository partRepository;

    public SynonymService(WordRepository wordRepository, PartRepository partRepository) {
        this.wordRepository = wordRepository;
        this.partRepository = partRepository;
    }

    public Map<String, List<Set<Long>>> sortExistedSynonymsToUniqueSets(Set<String> partOfSpeeches,
                                                                 Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId) {
        Map<String, List<Set<Long>>> posToSetsOfUncoupledUniqueSynonyms = new HashMap<>();

        partOfSpeeches.forEach(
                partOfSpeech -> {
                    List<Long> longs = partOfSpeechToExistedSynonymAsPartId.get(partOfSpeech); //это просто ids существующих синонимов
                    List<Set<Long>> setsOfUncoupledSynonyms = posToSetsOfUncoupledUniqueSynonyms.get(partOfSpeech);

                    longs.forEach(
                            ids -> {
                                if (!idExistsInList(setsOfUncoupledSynonyms, ids)) {
                                    Set<Long> synonymsIds = partRepository.findSynonymsByPartId(ids);
                                    synonymsIds.add(ids);
                                    setsOfUncoupledSynonyms.add(synonymsIds);
                                }
                            }
                    );
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
                            Long existedPartId;
                            if (existedPart.isPresent()) {
                                existedPartId = existedPart.get().getId();
                                existedSynonyms.add(existedPartId);
                            } else {
//                                Long partId = partRepository.addPartToWord(partOfSpeech, existedWord.getId());
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
        Word newSynonym = new Word();
        newSynonym.setName(syn);
        Part part = new Part();
        part.setName(partOfSpeech);
        newSynonym.setParts(Collections.singletonList(part));
        Word save = wordRepository.save(newSynonym);
        Long partId = save.getParts().get(0).getId();
        newSynonyms.add(partId);
    }

    Map<String, List<String>> preparePartToSynonymMap(WordRequest wordRequest) {
        Map<String, List<String>> partOfSpeechToSynonym = new HashMap<>();
        wordRequest.getParts()
                .forEach(
                        partDto -> {
                            ArrayList<String> synonyms = new ArrayList<>();
                            partDto.getSynonyms()
                                    .stream()
                                    .map(Part::getName)
                                    .forEach(synonyms::add);
                            partOfSpeechToSynonym.put(partDto.getName(), synonyms);
                        }
                );
        return partOfSpeechToSynonym;
    }

    public List<IdTuple> getExistedSynonymsPairsToBeCoupled(Map<String, List<Set<Long>>> synonymsToBeCoupled) {

        Map<String, List<Set<Long>>> copyOfSynonymsToBeCoupled = Map.copyOf(synonymsToBeCoupled);

        List<IdTuple> idTuples = new ArrayList<>();

        // TODO: 01.11.2022 можно сделвть 2 сета одинаковых - по одному идти, второй связывать
        copyOfSynonymsToBeCoupled.forEach((key, sets) -> sets.forEach(set -> {
                    sets.remove(set);
                    set.forEach(parentId -> sets.forEach(childIdSet -> childIdSet.forEach(childId -> idTuples.add(new IdTuple(parentId, childId)))));
                }
        ));

        return idTuples;

    }

    public Map<String, Set<Long>> getExistedSynonymsToBeCoupledWithNew(Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets) {
        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = new HashMap<>();

        partToExistedSynonymsUniqueSets.forEach((part, list) -> {
            list.forEach(set -> {
                Set<Long> longs = partToExistedSynonymsToBeCoupledWithNew.get(part);
                if (longs == null) {
                    partToExistedSynonymsToBeCoupledWithNew.put(part, set);
                } else {
                    longs.addAll(set);
                }
            });
        });
        return partToExistedSynonymsToBeCoupledWithNew;
    }


    // TODO: 01.11.2022 после связывания всех существующих синонимов - каждый из них имеет одинаковый набор -> можно добавлять новое только к одному из
    public List<IdTuple> getExistedSynonymsToNewToBeCoupled(Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> partOfSpeechToNewSynonymAsPartId) {
        List<IdTuple> idTuples = new ArrayList<>();

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
        return null;
    }

    public void coupleIds(List<IdTuple> idTuples) {
        idTuples.forEach(idTuple -> {
            partRepository.createReference(idTuple.getChild(), idTuple.getParent());
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
                partRepository.createReference(id, childId);
            });
        });
    }
}
