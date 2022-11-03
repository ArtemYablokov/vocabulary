package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.front.WordRequest;
import com.yablokovs.vocabulary.model.Word;

import java.util.List;

public interface WordServiceInterface {
    List<Word> findAllWords();

    Word saveNewWord(Word word);

    void updateWord(Word word);

    List<Word> getAllWordsByPrefix(String prefix);

    void coupleSynonyms(WordRequest wordRequest, Word word);
}
