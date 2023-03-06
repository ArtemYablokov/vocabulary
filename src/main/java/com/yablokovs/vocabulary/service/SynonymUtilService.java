package com.yablokovs.vocabulary.service;


import com.yablokovs.vocabulary.model.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SynonymUtilService {


    private final SynonymDAOService synonymDAOService;

    public SynonymUtilService(SynonymDAOService synonymDAOService) {
        this.synonymDAOService = synonymDAOService;
    }

    public Set<IdTuple> crossCoupleExistedSetsInternallyAsSyn(Map<String, Collection<Set<Long>>> synonymsToBeCoupled) {
        Set<IdTuple> idTuples = new HashSet<>();

        Map<String, List<Set<Long>>> setToListMapped =
                synonymsToBeCoupled.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().toList()));

        setToListMapped.forEach((part, remainingSets) -> {

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

    // TODO: 02/03/23 Check NPE in test
    public List<IdTuple> coupleExistedANTandSYNAsAnt(Map<String, Collection<Set<Long>>> existedSynonymsUniqueSets, Map<String, Collection<Set<Long>>> existedAntonymsUniqueSets) {
        // важно ли с какой стороны смотреть? нет - тк с какой стороны не начни - будут проверены все пары множеств ->
        // тогда юзаем БД Антонимов - тк менее нагруженная XDDDD

        List<IdTuple> idTuples = new ArrayList<>();

        existedSynonymsUniqueSets.forEach((part, listOfSynSets) -> {
            Collection<Set<Long>> listOfAntSets = existedAntonymsUniqueSets.get(part);
            if (!CollectionUtils.isEmpty(listOfAntSets)) {

                listOfSynSets.forEach(synSet -> {

                    // TODO: 02/03/23 move this call to DAO on upper level
                    Long firstWordFormFoundAntonyms = synonymDAOService.getAnyAntonymForSynSet(synSet);

                    listOfAntSets.forEach(antSet -> {
                        if (!antSet.contains(firstWordFormFoundAntonyms)) {
                            idTuples.addAll(crossCouple2Sets(synSet, antSet));
                        }
                    });
                });
            }
        });
        return idTuples;
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

    public List<IdTuple> crossCoupleNewInternally(Map<String, List<Long>> newSynOrAntPartIds) {
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
    public Map<String, List<Long>> flatMapNotDuplicatingSetsToOneSet(Map<String, Collection<Set<Long>>> partToExistedSynonymsNotDublicatingSets) {
        return partToExistedSynonymsNotDublicatingSets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().flatMap(Collection::stream).toList()));
    }

}