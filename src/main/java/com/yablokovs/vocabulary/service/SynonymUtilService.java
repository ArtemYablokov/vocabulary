package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SynonymUtilService {

    public Map<String, Collection<Set<Long>>> filterToUniqueSets(Map<String, Collection<Set<Long>>> setsOfExistedSynonymsToBeCoupledAsSynonyms) {
        return setsOfExistedSynonymsToBeCoupledAsSynonyms.entrySet()
                .stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> entry.getValue()
                                        .stream()
                                        .distinct()
                                        .collect(Collectors.toList())));
    }

    // TODO: 06/03/23 UTIL
    public <T> Map<String, Collection<T>> merge2maps(Map<String, Collection<T>> allAddedSynonyms, Map<String, Collection<T>> allAddedAntonyms) {
        Map<String, Collection<T>> merged = new HashMap<>(allAddedAntonyms);
        allAddedSynonyms.forEach((part, synonyms) -> {
            merged.merge(part, synonyms, (ant, syn) -> Stream.concat(ant.stream(), syn.stream()).collect(Collectors.toSet()));
        });

        return merged;
    }

    public Set<IdTuple> crossCoupleExistedSetsAsSyn(Map<String, Collection<Set<Long>>> synonymsToBeCoupled) {
        Set<IdTuple> idTuples = new HashSet<>();

        Map<String, List<Set<Long>>> collectionToOrderedListMapped =
                synonymsToBeCoupled.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().toList()));

        collectionToOrderedListMapped.forEach((part, remainingSets) -> {

            for (int i = 0; i < remainingSets.size() - 1; i++) {
                Set<Long> currentSet = remainingSets.get(i);
                for (Long currentId : currentSet) {
                    for (int j = i + 1; j < remainingSets.size(); j++) {
                        Set<Long> remainSet = remainingSets.get(j);
                        remainSet.forEach(remainId -> idTuples.add(new IdTuple(currentId, remainId)));
                    }
                }
            }
        });
        return idTuples;
    }

    public List<IdTuple> crossCouple2Sets(Set<Long> synSet, Set<Long> antSet) {
        List<IdTuple> coupledIds = new ArrayList<>();
        synSet.forEach(syn ->
                antSet.forEach(ant -> coupledIds.add(new IdTuple(syn, ant))));
        return coupledIds;
    }

    /*
     * Can be NEW-NEW or NEW-Existed
     * */
    public List<IdTuple> crossCouple2Lists(Map<String, List<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> newSynOrAntPartIds) {
        List<IdTuple> idTuples = new ArrayList<>();

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

    public List<IdTuple> crossCoupleMembersOfList(Map<String, List<Long>> newSynOrAntPartIds) {
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

    // 8 add NEW WORD itself to PARTs IDs to be created
    public Map<String, List<Long>> addNewWordPartsToNewSynonymsPartIds(Word word, Map<String, List<Long>> newSynonymsPartIds) {

        Map<String, List<Long>> newSynonymsPartIdsWithWord = new HashMap<>(newSynonymsPartIds);

        word.getParts().forEach(part -> {
            String partName = part.getName();

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
    public Map<String, List<Long>> flatMapNotDuplicatingSetsToOneSet(Map<String, Collection<Set<Long>>> uniqueSynonymsSets) {
        return uniqueSynonymsSets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().flatMap(Collection::stream).toList()));
    }

}
