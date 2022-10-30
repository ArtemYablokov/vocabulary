package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.model.Prefix;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PrefixRepository;
import com.yablokovs.vocabulary.repo.PrefixToWordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PrefixService {

    @Autowired
    PrefixRepository prefixRepository;

    @Autowired
    PrefixToWordRepo prefixToWordRepo;

    public void synchronisePrefixesForWordWithoutAddingWordToPrefixSet(Word word) {
        List<Prefix> prefixesFromWord = getPrefixesFromWord(word.getName());
        prefixesFromWord.forEach(prefix -> {
            Optional<Prefix> prefixOptional = prefixRepository.findByName(prefix.getName());
            if (prefixOptional.isEmpty()) {
                Prefix save = prefixRepository.save(prefix);
                prefixToWordRepo.addWordsToPrefix(save.getId(), word.getId());
            } else {
                prefixToWordRepo.addWordsToPrefix(prefixOptional.get().getId(), word.getId());
            }
            // TODO: 29.10.2022 sonarlint
            System.out.println("--------end--------");
        });
    }

    private List<Prefix> getPrefixesFromWord(String wordName) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Prefix> prefixes = new ArrayList<>();

        char[] chars = wordName.toCharArray();
        for (char ch : chars) {
            StringBuilder append = stringBuilder.append(ch);
            Prefix prefix = new Prefix();
            prefix.setName(append.toString());
            prefixes.add(prefix);
        }
        return prefixes;
    }

    // TODO: 28.10.2022 easy to optimise: 1 select prefix 2 if not exists - create(get ID) 3 add prefixID-WordID
    // DONE in synchronisePrefixesForWordWithoutAddingWordToPrefixSet(Word word)
    public void synchronisePrefixesForWord(Word word) {
        List<String> prefixesFromWord = getStringPrefixesFromWord(word.getName());
        prefixesFromWord.forEach(prefix -> {
            Optional<Prefix> prefixOptional = prefixRepository.findByName(prefix);
            Prefix prefixToSave;
            if (prefixOptional.isPresent()) {
                prefixToSave = prefixOptional.get();
                prefixToSave.getWords().add(word);
            } else {
                prefixToSave = new Prefix();
                prefixToSave.setName(prefix);
                prefixToSave.setWords(Collections.singleton(word));
            }
            prefixRepository.save(prefixToSave);
            System.out.println("--------end--------");
        });
    }


    private List<String> getStringPrefixesFromWord(String wordName) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> prefixes = new ArrayList<>();

        char[] chars = wordName.toCharArray();
        for (char ch : chars) {
            StringBuilder append = stringBuilder.append(ch);
            prefixes.add(append.toString());
        }
        return prefixes;
    }

    public void save(Prefix prefix) {
        prefixRepository.save(prefix);
    }

    public List<Prefix> findAll() {
        return prefixRepository.findAll();
    }
}
