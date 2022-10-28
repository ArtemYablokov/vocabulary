package com.yablokovs.vocabulary.controller;

import com.yablokovs.vocabulary.mdto.front.WordDto;
import com.yablokovs.vocabulary.mdto.front.mapper.WordMapper;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.service.WordServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
public class WordController {

    // TODO: 22.10.2022 с помощью имени бина можно задавать имплементацию (кроме @Primary и @Qualifier)
    private final WordServiceInterface wordService;
    private final WordMapper wordMapper;

    public WordController(WordServiceInterface wordService, WordMapper wordMapper1) {
        this.wordService = wordService;
        this.wordMapper = wordMapper1;
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

    @PutMapping("/new")
    public ResponseEntity<String> newWord(@RequestBody WordDto wordDto) {
        Word word = wordMapper.toWord(wordDto);
        wordService.saveNewWord(word);
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
}
