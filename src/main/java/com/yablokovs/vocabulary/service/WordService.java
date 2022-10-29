package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Prefix;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WordService implements WordServiceInterface {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Override
    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    @Override
    public void saveNewWord(Word word) {
        word.setNumberOfSearches(1L);
        setWordAndPartParents(word);

        // TODO: 28.10.2022 prefixes shouldn't be set to Word -> before saving each prefix should check if it already exists in DB

        wordRepository.save(word);
    }

    private List<Prefix> getPrefixesFromWord(Word word) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Prefix> prefixes = new ArrayList<>();
        String name = word.getName();
        char[] chars = name.toCharArray();
        for (char ch : chars) {
            StringBuilder append = stringBuilder.append(ch);
            Prefix prefix = new Prefix();
            prefix.setName(append.toString());
            prefixes.add(prefix);
        }
        return prefixes;
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
}
