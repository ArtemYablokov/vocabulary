package com.yablokovs.vocabulary.controller;

import com.yablokovs.vocabulary.model.Word;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yablokovs.vocabulary.service.WordServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class WordController {

    // TODO: 22.10.2022 с помощью имени бина можно задавать имплементацию (кроме @Primary и @Qualifier)
    private final WordServiceInterface wordService;

    public WordController(WordServiceInterface wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/find")
    public ResponseEntity<List<String>> findWord(@RequestParam String prefix) {
        // imitation of using DB
        Map<String, List<String>> vocabulary = new HashMap<>();
        vocabulary.put("w", List.of("n-th letter of english alphabet", "other"));
        vocabulary.put("word", List.of("indivisible part of sentence", "other"));
        List<String> result = vocabulary.get(prefix);

        List<Word> allWordsByPrefix = wordService.getAllWordsByPrefix(prefix);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<String> addWord(@RequestBody WordRequest word) {
        // imitation of using DB
        Map<String, List<String>> vocabulary = new HashMap<>();
        vocabulary.put("w", List.of("n-th letter of english alphabet", "other"));
        vocabulary.put("word", List.of("indivisible part of sentence", "other"));

        Word word1 = new Word();
        word1.setName(word.name);
        wordService.saveWord(word1);
        return new ResponseEntity<>("result", HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseOk> healthCheck() {
        return new ResponseEntity<>(new ResponseOk("ok message"), HttpStatus.OK);
    }

    private class ResponseOk {

        private final String message;

        public ResponseOk(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void text() {
            log.info("\n");
        }
    }

    private static class WordRequest {

        private String name;
        private String definition;

        public WordRequest(String name, String definition) {
            this.name = name;
            this.definition = definition;
        }

        public String getName() {
            return name;
        }

        public String getDefinition() {
            return definition;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}
