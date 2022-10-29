package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.repo.PrefixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrefixService {

    @Autowired
    PrefixRepository prefixRepository;

    public void synchronisePrefixesForWord(String word) {




    }
}
