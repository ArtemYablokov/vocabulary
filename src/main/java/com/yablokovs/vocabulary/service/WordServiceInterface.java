package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Word;

import java.util.List;

public interface WordServiceInterface {
    List<Word> findAllWords();

    void saveWord(Word word);

    List<Word> getAllWordsByPrefix(String prefix);
}
