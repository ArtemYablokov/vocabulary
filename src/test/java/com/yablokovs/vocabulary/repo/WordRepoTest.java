package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Part;
import com.yablokovs.vocabulary.model.Word;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WordRepoTest {

    @Autowired
    WordRepository wordRepository;


    @Test
//    @Transactional // not use here !!!
    void findAllByNameInTest() {

        Word word1 = getWord(1);
        Word word2 = getWord(2);
        Word word22 = getWord(2);
        Word word3 = getWord(3);

        wordRepository.saveAll(List.of(word1, word2, word22, word3));

        Set<Word> allWhereNameIn = wordRepository.findAllByNameIn(Set.of("word1", "word2"));

//        assertThat(allWhereNameIn).isSameAs();
        int n = 0;
    }

    private static Word getWord(int n) {
        Word word = new Word(String.format("word%d", n));
        word.addPart(new Part(String.format("part%d", n)));
        return word;
    }
}