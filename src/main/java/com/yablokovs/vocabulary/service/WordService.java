package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordService implements WordServiceInterface {

    private final WordRepository wordRepository;
    private final SynonymService synonymService;

    public WordService(WordRepository wordRepository, SynonymService synonymService) {
        this.wordRepository = wordRepository;
        this.synonymService = synonymService;
    }

    @Override
    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    @Override
    public Word saveNewWord(Word word) {
        word.setNumberOfSearches(1L);
        setWordAndPartParents(word);
        return wordRepository.save(word);
    }

    private void setWordAndPartParents(Word word) {
        word.getParts().forEach(part -> {
            part.setWord(word);
            part.getDefinitions().forEach(definition -> {
                definition.setPart(part);
            });
        });
    }

    @Override
    public void updateWord(Word word) {
        Long numberOfSearches = word.getNumberOfSearches();
        word.setNumberOfSearches(++numberOfSearches);
        wordRepository.save(word);
    }

    @Override
    public List<Word> getAllWordsByPrefix(String prefix) {
        // TODO: 16.10.2022 IMPLEMENT
        // return wordRepository.findByName(name).orElseThrow(RuntimeException::new); OPTIONAL???
        return new ArrayList<>();
    }

    @Override
    public void coupleSynonyms(WordRequest wordRequest, Word word) {

        // TODO: 03.11.2022 corner case when there is no synonyms

        Map<String, List<String>> partOfSpeechToSynonym = synonymService.preparePartToSynonymMap(wordRequest);

        // TODO: 30.10.2022 мапа хранит PoS - на Ids. потом к этим id по PoS нужно добавить все синонимы
        Map<String, List<Long>> newSynonyms = new HashMap<>();
        Map<String, List<Long>> existedSynonyms = new HashMap<>();

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
