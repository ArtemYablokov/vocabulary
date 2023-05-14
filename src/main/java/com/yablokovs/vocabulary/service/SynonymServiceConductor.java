package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.model.WordRus;
import com.yablokovs.vocabulary.service.rus.WordRusDao;
import com.yablokovs.vocabulary.service.rus.WordRusService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * API pattern - to hide more complex implementation
 */
@Service
public class SynonymServiceConductor {

    private final WordDao wordDao;
    private final SynonymUtilService synonymUtilService;
    private final WordRusDao wordRusDao;
    private final WordRusService wordRusService;
    private final DAOService daoService;
    private final FirstLevelService firstLevelService;

    public SynonymServiceConductor(SynonymUtilService synonymUtilService, WordDao wordDao, WordRusDao wordRusDao, WordRusService wordRusService, DAOService daoService, FirstLevelService firstLevelService) {
        this.synonymUtilService = synonymUtilService;
        this.wordDao = wordDao;
        this.wordRusDao = wordRusDao;
        this.wordRusService = wordRusService;
        this.daoService = daoService;
        this.firstLevelService = firstLevelService;
    }

    public void coupleSynAndAntNewImplementation(WordFrontEnd wordFrontEnd, Word word) {

        // RUS --- START
        Map<String, Collection<String>> basicRusSynMap = firstLevelService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getRusSynonyms);
        Map<String, Collection<String>> basicRusAntMap = firstLevelService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getRusAntonyms);

        Set<WordRus> existedSynAndAntRusFromRepo = wordRusDao.findAllRusWordsBySynOrAntStrings(flatMapAllSynonymsToOneSet(synonymUtilService.merge2maps(basicRusSynMap, basicRusAntMap)));

        Map<String, Collection<WordRus>> existedSynRus = wordRusService.getExistedWordRus(basicRusSynMap, existedSynAndAntRusFromRepo);
        Map<String, Collection<WordRus>> existedAntRus = wordRusService.getExistedWordRus(basicRusAntMap, existedSynAndAntRusFromRepo);

        // EXISTED
        Map<String, Collection<Set<Long>>> uniqueSetsOfSynRusWithAntOfAnt = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynRus, existedAntRus);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntRusWithAntOfSyn = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntRus, existedSynRus);

        // NEW
        Map<String, List<Long>> newSynRus = wordRusService.getNewSyn_AntWordRusIds(basicRusSynMap, existedSynAndAntRusFromRepo);
        Map<String, List<Long>> newAntRus = wordRusService.getNewSyn_AntWordRusIds(basicRusAntMap, existedSynAndAntRusFromRepo);

        List<IdTuple> coupledRusAntonyms = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(uniqueSetsOfSynRusWithAntOfAnt, uniqueSetsOfAntRusWithAntOfSyn, newSynRus, newAntRus, DataExtractorFunctionName.GET_RUS_ANTONYM_SET_BY_SYNONYM);
        List<IdTuple> coupledRUSSynonymsAsSynonyms = firstLevelService.coupleExistedAndNewAsSynonyms(uniqueSetsOfSynRusWithAntOfAnt, newSynRus);
        List<IdTuple> coupledRUSAntonymsAsSynonyms = firstLevelService.coupleExistedAndNewAsSynonyms(uniqueSetsOfAntRusWithAntOfSyn, newAntRus);
        // RUS --- END

        // ENG --- START
        Map<String, Collection<String>> basicSynonymsMap = firstLevelService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        Map<String, Collection<String>> basicAntonymsMap = firstLevelService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);

        Set<Word> existedSynonymsAndAntonymsFromRepo = wordDao.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(synonymUtilService.merge2maps(basicSynonymsMap, basicAntonymsMap)));

        Map<String, Collection<Part>> existedSynonyms = firstLevelService.getExistedParts(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, Collection<Part>> existedAntonyms = firstLevelService.getExistedParts(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // EXISTED
        Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsWithAntOfAnt = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynonyms, existedAntonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsWithAntOfSyn = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntonyms, existedSynonyms);

        // NEW
        Map<String, List<Long>> newSynonymsIncludingWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(
                word, firstLevelService.getNewSyn_AntPartIds(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo));
        Map<String, List<Long>> newAntonyms = firstLevelService.getNewSyn_AntPartIds(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        List<IdTuple> antonymsToIdTuples = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(
                uniqueSetsOfSynonymsWithAntOfAnt, uniqueSetsOfAntonymsWithAntOfSyn, newSynonymsIncludingWord, newAntonyms, DataExtractorFunctionName.GET_ENG_ANTONYM_SET_BY_SYNONYM);
        List<IdTuple> coupledSynonymsAsSynonyms = firstLevelService.coupleExistedAndNewAsSynonyms(uniqueSetsOfSynonymsWithAntOfAnt, newSynonymsIncludingWord);
        List<IdTuple> coupledAntonymsAsSynonyms = firstLevelService.coupleExistedAndNewAsSynonyms(uniqueSetsOfAntonymsWithAntOfSyn, newAntonyms);
        // ENG --- END


        // TODO: 5/13/23 слева должен быть RUS, справа ENG !!!! --------------------------------- !!!!!!!!!!!!!!!!!!!!
        // RUS - ENG syn as SYN
        List<IdTuple> coupledRusAndEngSynAsSyn = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(uniqueSetsOfSynRusWithAntOfAnt, uniqueSetsOfSynonymsWithAntOfAnt, newSynRus, newSynonymsIncludingWord, DataExtractorFunctionName.FIND_ENG_SYNONYMS_BY_RUS_WORD_ID);
        // RUS - ENG ant as SYN
        List<IdTuple> coupledRusAndEngAntAsSyn = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(uniqueSetsOfAntRusWithAntOfSyn, uniqueSetsOfAntonymsWithAntOfSyn, newAntRus, newAntonyms, DataExtractorFunctionName.FIND_ENG_SYNONYMS_BY_RUS_WORD_ID);
        // RUS syn - ENG ant as ANT
        List<IdTuple> coupledRusSynAndEngAntAsAnt = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(
                uniqueSetsOfSynRusWithAntOfAnt, uniqueSetsOfAntonymsWithAntOfSyn, newSynRus, newAntonyms, DataExtractorFunctionName.FIND_ENG_ANTONYMS_BY_RUS_WORD_ID);
        // RUS ant - ENG syn as ANT
        List<IdTuple> coupledRusAntAndEngSynAsAnt = firstLevelService.coupleSynonymsAndAntonymsAsAntonyms(
                uniqueSetsOfAntRusWithAntOfSyn, uniqueSetsOfSynonymsWithAntOfAnt, newAntRus, newSynonymsIncludingWord, DataExtractorFunctionName.FIND_ENG_ANTONYMS_BY_RUS_WORD_ID);


        daoService.saveRusWordToEngPartAsAntonymIdTuple(coupledRusSynAndEngAntAsAnt);
        daoService.saveRusWordToEngPartAsAntonymIdTuple(coupledRusAntAndEngSynAsAnt);

        daoService.saveRusWordToEngPartAsSynonymIdTuple(coupledRusAndEngSynAsSyn);
        daoService.saveRusWordToEngPartAsSynonymIdTuple(coupledRusAndEngAntAsSyn);

        daoService.saveRusAntonymIdTuple(coupledRusAntonyms);
        daoService.saveRusSynonymIdTuple(coupledRUSSynonymsAsSynonyms);
        daoService.saveRusSynonymIdTuple(coupledRUSAntonymsAsSynonyms);

        daoService.saveEngAntonymIdTuple(antonymsToIdTuples);
        daoService.saveEngSynonymIdTuple(coupledSynonymsAsSynonyms);
        daoService.saveEngSynonymIdTuple(coupledAntonymsAsSynonyms);
    }

    public List<String> flatMapAllSynonymsToOneSet(Map<String, Collection<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).toList();
    }
}
