package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SynonymUtilService {

    // TODO: 4/6/23 only one usage of DAO
    private final SynonymDAOService synonymDAOService;

    public SynonymUtilService(SynonymDAOService synonymDAOService) {
        this.synonymDAOService = synonymDAOService;
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

    // TODO: 05/03/23 REFACTOR - метод гавно (NPE)
    public List<IdTuple> coupleExistedAntAndSynAsAnt(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets,
                                                     Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets) {
        List<IdTuple> idTuples = new ArrayList<>();

        existedSynonymsUniqueSets.forEach((part, listOfSynSets) -> {
            Collection<Set<Long>> listOfAntSets = existedAntonymsUniqueSets.get(part);
            if (!CollectionUtils.isEmpty(listOfAntSets)) {

                listOfSynSets.forEach(synSet -> {
                    Long anySynonym = synSet.iterator().next();
                    Set<Long> foundAntonymsBySynonym = synonymDAOService.findAntonymsByPartId(anySynonym);

                    if (synonymHasAntonyms(foundAntonymsBySynonym)) {
                        Long anyFoundAntonym = foundAntonymsBySynonym.iterator().next();

                        listOfAntSets.forEach(antSet -> {
                            if (!antSet.contains(anyFoundAntonym)) {
                                idTuples.addAll(crossCouple2Sets(synSet, antSet));
                            }
                        });
                    } else {
                        listOfAntSets.forEach(antSet -> idTuples.addAll(crossCouple2Sets(synSet, antSet)));
                    }
                });
            }
        });
        return idTuples;
    }

    private boolean synonymHasAntonyms(Set<Long> foundAntonymsBySynonym) {
        return !CollectionUtils.isEmpty(foundAntonymsBySynonym);
    }

    private List<IdTuple> crossCouple2Sets(Set<Long> synSet, Set<Long> antSet) {
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
