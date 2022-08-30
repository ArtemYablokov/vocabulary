package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.WordRepository;
import org.springframework.stereotype.Service;

@Service
public class WordService {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public Word getWordByName(String name){
        return wordRepository.findByName(name).orElseThrow(RuntimeException::new);
    }

    public Word saveWord(Word word){
        return wordRepository.save(word);
    }
}
