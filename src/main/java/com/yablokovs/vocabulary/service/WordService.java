package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Prefix;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PrefixRepository;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class WordService implements WordServiceInterface {

    private final WordRepository wordRepository;
    private final PrefixRepository prefixRepository;

    public WordService(WordRepository wordRepository, PrefixRepository prefixRepository) {
        this.wordRepository = wordRepository;
        this.prefixRepository = prefixRepository;
    }

    @Override
    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    @Override
    public Word saveNewWordWithPartsAndDefinitions(Word word) {
        word.setNumberOfSearches(1L);
        setParentsForPartAndDefinition(word);
        return wordRepository.save(word);
    }

    private void setParentsForPartAndDefinition(Word word) {
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
        List<Word> wordsByPrefix = new ArrayList<>();
        Optional<Prefix> byName = prefixRepository.findByName(prefix);
        byName.ifPresent(p -> wordsByPrefix.addAll(p.getWords())); // TODO: 18.11.2022 N+1
        return wordsByPrefix;
    }

    public Word save(Word newSynonym) {
        return wordRepository.save(newSynonym);
    }

    public Optional<Word> findByName(String syn) {
        return wordRepository.findByName(syn);
    }

    public Set<Word> findAllWordsWithPartsBySynonymsStrings(Set<String> synonyms) {
        return wordRepository.findAllByNameIn(synonyms);
    }

    public List<Word> saveAllWords(List<Word> mergedNewWordsToBeSaved) {
        return wordRepository.saveAll(mergedNewWordsToBeSaved);
    }
}
