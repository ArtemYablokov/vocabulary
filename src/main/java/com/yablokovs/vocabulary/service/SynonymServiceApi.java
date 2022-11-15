package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* API pattern - to hide more complex implementation
* */
@Service
public class SynonymServiceApi {

    SynonymService synonymService;

    public SynonymServiceApi(SynonymService synonymService) {
        this.synonymService = synonymService;
    }

    public void coupleSynonyms(WordRequest wordRequest, Word word) {

        // TODO: 03.11.2022 corner case when there is no synonyms

        Map<String, List<String>> partOfSpeechToSynonym = synonymService.preparePartToSynonymMap(wordRequest);

        // TODO: 30.10.2022 мапа хранит PoS - на Ids. потом к этим id по PoS нужно добавить все синонимы
        Map<String, List<Long>> newSynonyms = new HashMap<>();
        Map<String, List<Long>> existedSynonyms = new HashMap<>();

        // TODO: 04.11.2022 bug -> not coupled a(c) with existed b(c)
        synonymService.prepareExistedAndNewPartOfSpeechIds(partOfSpeechToSynonym, newSynonyms, existedSynonyms);

        word.getParts().forEach(part -> newSynonyms.get(part.getName()).add(part.getId()));

        Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets = synonymService.filterExistedSynonymsToUniqueSets(partOfSpeechToSynonym.keySet(), existedSynonyms);

        Map<String, Set<Long>> partToExistedSynonymsToBeCoupledWithNew = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsUniqueSets);

        // TODO: 01.11.2022 argument is modified - so need to pass a copy !
        Set<IdTuple> existedSynonymsToBeCoupled = synonymService.getPairsOfExistedSynonymsToBeCoupled(partToExistedSynonymsUniqueSets);

        Set<IdTuple> existedSynonymsToNewToBeCoupled = synonymService.getExistedSynonymsToNewToBeCoupled(partToExistedSynonymsToBeCoupledWithNew, newSynonyms);

        List<IdTuple> newSynonymsToBeCoupled = synonymService.getNewSynonymsToBeCoupled(newSynonyms);

        synonymService.coupleIds(existedSynonymsToBeCoupled);
        synonymService.coupleIds(existedSynonymsToNewToBeCoupled);
        synonymService.coupleIds(newSynonymsToBeCoupled);

    }
}
