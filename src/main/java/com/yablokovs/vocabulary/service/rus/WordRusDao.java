package com.yablokovs.vocabulary.service.rus;

import com.yablokovs.vocabulary.model.WordRus;
import com.yablokovs.vocabulary.repo.WordRusRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordRusDao {

    private final WordRusRepository wordRusRepository;

    public WordRusDao(WordRusRepository wordRusRepository) {
        this.wordRusRepository = wordRusRepository;
    }

    public Set<WordRus> findAllRusWordsBySynOrAntStrings(Collection<String> synonyms) {
        return wordRusRepository.findAllByNameIn(synonyms);
    }
}
