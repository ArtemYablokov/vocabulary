package com.yablokovs.vocabulary.service;

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
    public void saveWord(Word word) {
        wordRepository.save(word);
    }

    @Override
    public List<Word> getAllWordsByPrefix(String prefix) {
        // TODO: 16.10.2022 IMPLEMENT
        // return wordRepository.findByName(name).orElseThrow(RuntimeException::new); OPTIONAL???
        return new ArrayList<>();
    }
}
