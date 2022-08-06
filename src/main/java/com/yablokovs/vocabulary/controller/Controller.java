package com.yablokovs.vocabulary.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {

    @GetMapping("/find")
    public ResponseEntity<List<String>> findWord(@RequestParam String prefix) {

        Map<String, List<String>> vocabulary = new HashMap<>();

        vocabulary.put("w", List.of("n-th letter of english alphabet", "other"));
        vocabulary.put("word", List.of("indivisible part of sentence", "other"));

        List<String> result = vocabulary.get(prefix);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<String> addWord(@RequestBody Word word) {

        Map<String, List<String>> vocabulary = new HashMap<>();

        vocabulary.put("w", List.of("n-th letter of english alphabet", "other"));
        vocabulary.put("word", List.of("indivisible part of sentence", "other"));

        return new ResponseEntity<>("result", HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseOk> healthCheck() {
        return new ResponseEntity<>(new ResponseOk("ok message"), HttpStatus.OK);
    }

    private record ResponseOk(String message) {
        public String getMessage() {
            return message;
        }

        public void text() {
            System.out.println();
        }
    }

    private static class Word {

        private String name;
        private String definition;

        public Word(String name, String definition) {
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
