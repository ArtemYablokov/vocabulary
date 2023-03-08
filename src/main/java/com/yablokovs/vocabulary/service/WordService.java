package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Prefix;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordService implements WordServiceInterface {

    private final WordRepository wordRepository;
    private final PrefixService prefixService;

    public WordService(WordRepository wordRepository, PrefixService prefixService) {
        this.wordRepository = wordRepository;
        this.prefixService = prefixService;
    }

    @Deprecated
    @Override
    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    @Override
    public Word saveNewWord(Word word) {

        setParentsForPartAndDefinition(word);
        wordRepository.save(word);
        prefixService.saveOrCouplePrefixesIdToNewWordId(word);
        return word;
    }

    private void setParentsForPartAndDefinition(Word word) {
        word.getParts().forEach(part -> {
            part.setWord(word);
            part.getDefinitions().forEach(definition -> definition.setPart(part));
        });
    }

    @Override
    public List<Word> getAllWordsByPrefix(String prefix) {
        List<Word> wordsByPrefix = new ArrayList<>();
        Optional<Prefix> byName = prefixService.findByName(prefix);

        // TODO: 20.11.2022 почему WORD доступны - сессия должна же быть закрыта?
        // TODO: 20.11.2022 каким-то образом потом выполняются все запросы для наполнения WORD
        byName.ifPresent(p -> wordsByPrefix.addAll(p.getWords())); // TODO: 18.11.2022 N+1
        return wordsByPrefix;
    }

    @Override
    public void updateWord(Word word) {
        // TODO: 20.11.2022 IMPL
//        Long numberOfSearches = word.getNumberOfSearches();
//        word.setNumberOfSearches(++numberOfSearches);
//        wordRepository.save(word);
    }

    /**
     * @Deprecated because there is no logic yet, where you need simply to save Word (w/out prefixes or synonyms)
     */
    @Deprecated
    public Word save(Word newSynonym) {
        return wordRepository.save(newSynonym);
    }

    /**
     * @Deprecated because there is no logic yet, where you need simply to get Word by name (always by PREFIX)
     */
    @Deprecated
    public Optional<Word> findByName(String syn) {
        return wordRepository.findByName(syn);
    }

    public Set<Word> findAllWordsWithPartsBySynOrAntStrings(Collection<String> synonyms) {
        return wordRepository.findAllByNameIn(synonyms);
    }

    public List<Word> saveAllNewWords(List<Word> newWordsToBeSaved) {
        newWordsToBeSaved.forEach(this::setParentsForPartAndDefinition);

        wordRepository.saveAll(newWordsToBeSaved);

        newWordsToBeSaved.forEach(prefixService::saveOrCouplePrefixesIdToNewWordId);

        return newWordsToBeSaved;
    }

    public List<Word> updateAllWords(List<Word> wordsToBeUpdatedWithNewParts) {
        return wordRepository.saveAll(wordsToBeUpdatedWithNewParts);
    }
}
