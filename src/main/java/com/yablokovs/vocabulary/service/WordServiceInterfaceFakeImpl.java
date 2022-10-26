package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Word;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordServiceInterfaceFakeImpl implements WordServiceInterface {
    @Override
    public List<Word> findAllWords() {
        return null;
    }

    @Override
    public void saveWord(Word word) {

    }

    @Override
    public List<Word> getAllWordsByPrefix(String prefix) {
        return null;
    }
}
