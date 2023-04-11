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

    private final SynonymService synonymService;
    private final WordDao wordDao;
    private final SynonymUtilService synonymUtilService;

    private final WordRusDao wordRusDao;
    private final WordRusService wordRusService;

    private final SynonymDAOService synonymDAOService;

    private final FirstLevelService firstLevelService;

    private final SynonymUtil synonymUtil;

    public SynonymServiceConductor(SynonymService synonymService, SynonymUtilService synonymUtilService, WordDao wordDao, WordRusDao wordRusDao, WordRusService wordRusService, SynonymDAOService synonymDAOService, FirstLevelService firstLevelService, SynonymUtil synonymUtil) {
        this.synonymService = synonymService;
        this.synonymUtilService = synonymUtilService;
        this.wordDao = wordDao;
        this.wordRusDao = wordRusDao;
        this.wordRusService = wordRusService;
        this.synonymDAOService = synonymDAOService;
        this.firstLevelService = firstLevelService;
        this.synonymUtil = synonymUtil;
    }

    public void coupleSynAndAntNewImplementation(WordFrontEnd wordFrontEnd, Word word) {


        // RUS
        Map<String, Collection<String>> basicRusSynMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getRusSynonyms);
        Map<String, Collection<String>> basicRusAntMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getRusAntonyms);

        // RUS
        Set<WordRus> existedSynAndAntRusFromRepo = wordRusDao.findAllRusWordsBySynOrAntStrings(flatMapAllSynonymsToOneSet(synonymUtil.merge2maps(basicRusSynMap, basicRusAntMap)));

        // EXISTED RUS
        Map<String, Collection<WordRus>> existedSynRus = wordRusService.getExistedWordRus(basicRusSynMap, existedSynAndAntRusFromRepo);
        Map<String, Collection<WordRus>> existedAntRus = wordRusService.getExistedWordRus(basicRusAntMap, existedSynAndAntRusFromRepo);


        // EXISTED with ANT of ANT
        Map<String, Collection<Set<Long>>> uniqueSetsOfSynRusWithAntOfAnt = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynRus, existedAntRus);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntRusWithAntOfSyn = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntRus, existedSynRus);
        // TODO: 4/9/23 tested correctly created unique sets of syn + ant of ant. (and sets of ant + ant of syn )


        Map<String, List<Long>> newSynRus = wordRusService.getNewSyn_AntWordRusIds(basicRusSynMap, existedSynAndAntRusFromRepo);
        Map<String, List<Long>> newAntRus = wordRusService.getNewSyn_AntWordRusIds(basicRusAntMap, existedSynAndAntRusFromRepo);
        // TODO: 4/10/23 TEST !!!



        // ENG

        Map<String, Collection<String>> basicSynonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getSynonyms);
        Map<String, Collection<String>> basicAntonymsMap = synonymService.getBasicMapOfPartToSynOrAnt(wordFrontEnd, PartDto::getAntonyms);

        Set<Word> existedSynonymsAndAntonymsFromRepo = wordDao.findAllWordsWithPartsBySynOrAntStrings(flatMapAllSynonymsToOneSet(synonymUtil.merge2maps(basicSynonymsMap, basicAntonymsMap)));

        // EXISTED
        Map<String, Collection<Part>> existedSynonyms = synonymService.getExistedParts(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo);
        Map<String, Collection<Part>> existedAntonyms = synonymService.getExistedParts(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // EXISTED with ANT of ANT
        Map<String, Collection<Set<Long>>> uniqueSetsOfSynonymsWithAntOfAnt = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedSynonyms, existedAntonyms);
        Map<String, Collection<Set<Long>>> uniqueSetsOfAntonymsWithAntOfSyn = firstLevelService.getUniqueSetsOfSyn_AntWithAntOfAnt_Syn(existedAntonyms, existedSynonyms);

        // NEW
        Map<String, List<Long>> newSynonymsIncludingWord = synonymUtilService.addNewWordPartsToNewSynonymsPartIds(
                word, firstLevelService.getNewSyn_AntPartIds(basicSynonymsMap, existedSynonymsAndAntonymsFromRepo));
        Map<String, List<Long>> newAntonyms = firstLevelService.getNewSyn_AntPartIds(basicAntonymsMap, existedSynonymsAndAntonymsFromRepo);

        // PREPARED TUPLES
        List<IdTuple> antonymsToIdTuples = synonymService.coupleSynonymsAndAntonymsAsAntonyms(
                uniqueSetsOfSynonymsWithAntOfAnt, uniqueSetsOfAntonymsWithAntOfSyn, newSynonymsIncludingWord, newAntonyms);
        List<IdTuple> coupledSynonymsAsSynonyms = synonymService.coupleSynonymsToIdTuples(uniqueSetsOfSynonymsWithAntOfAnt, newSynonymsIncludingWord);
        List<IdTuple> coupledAntonymsAsSynonyms = synonymService.coupleSynonymsToIdTuples(uniqueSetsOfAntonymsWithAntOfSyn, newAntonyms);

        synonymDAOService.saveAntonymIdTuple(antonymsToIdTuples);
        synonymDAOService.saveSynonymIdTuple(coupledSynonymsAsSynonyms);
        synonymDAOService.saveSynonymIdTuple(coupledAntonymsAsSynonyms);
    }

    public List<String> flatMapAllSynonymsToOneSet(Map<String, Collection<String>> basicPartToSYNmap) {
        return basicPartToSYNmap.values().stream().flatMap(Collection::stream).toList();
    }
}
