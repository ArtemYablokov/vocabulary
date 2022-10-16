package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.WordRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WordService {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }

    public void saveWord(Word word) {
        wordRepository.save(word);
    }

    public List<Word> getAllWordsByPrefix(String prefix) {
        // TODO: 16.10.2022 IMPLEMENT
        // return wordRepository.findByName(name).orElseThrow(RuntimeException::new); OPTIONAL???
        return new ArrayList<>();
    }
}
