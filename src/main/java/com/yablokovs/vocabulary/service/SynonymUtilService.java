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


    // TODO: 01.11.2022 argument is modified - so need to use a copy !
    // TODO: 03.11.2022 concurrent COLLECTIONS can be used w/out iterator for removing
    public Set<IdTuple> crossSetsToCoupleEachMemberOfEachSet(Map<String, List<Set<Long>>> synonymsToBeCoupled) {

        Map<String, List<Set<Long>>> copyOfSynonymsToBeCoupled = Map.copyOf(synonymsToBeCoupled);

        Set<IdTuple> idTuples = new HashSet<>();

        copyOfSynonymsToBeCoupled.forEach((part, remainingSets) -> {
            Iterator<Set<Long>> iterator = remainingSets.iterator();

            while (iterator.hasNext()) {
                Set<Long> currentSet = iterator.next();
                iterator.remove();

                currentSet.forEach(headId ->
                        remainingSets.forEach(childIdSet ->
                                childIdSet.forEach(childId ->
                                        idTuples.add(new IdTuple(headId, childId)))));
            }
        });
        return idTuples;
    }

    // TODO: 02/03/23 Check NPE in test
    public List<IdTuple> coupleExistedANTandSYN(Map<String, List<Set<Long>>> existedSynonymsUniqueSets, Map<String, List<Set<Long>>> existedAntonymsUniqueSets) {
        // важно ли с какой стороны смотреть? нет - тк с какой стороны не начни - будут проверены все пары множеств ->
        // тогда юзаем БД Антонимов - тк менее нагруженная XDDDD

        List<IdTuple> idTuples = new ArrayList<>();

        existedSynonymsUniqueSets.forEach((part, listOfSynSets) -> {
            List<Set<Long>> listOfAntSets = existedAntonymsUniqueSets.get(part);
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

    public List<IdTuple> crossCouple2ListsOfId(Map<String, List<Long>> partToExistedSynonymsToBeCoupledWithNew, Map<String, List<Long>> newSynOrAntPartIds) {
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

    public List<IdTuple> getAllPairsToBeCoupled(Map<String, List<Long>> newSynOrAntPartIds) {
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
    public Map<String, List<Long>> flatMapNotDuplicatingSetsToOneSet(Map<String, List<Set<Long>>> partToExistedSynonymsNotDublicatingSets) {
        return partToExistedSynonymsNotDublicatingSets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().flatMap(Collection::stream).toList()));
    }

}
