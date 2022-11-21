package com.yablokovs.vocabulary.rest;

import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.mdto.request.mapper.WordMapper;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PhraseRepository;
import com.yablokovs.vocabulary.service.SynonymServiceApi;
import com.yablokovs.vocabulary.service.WordServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
public class WordController {

    // TODO: 22.10.2022 с помощью имени бина можно задавать имплементацию (кроме @Primary и @Qualifier)
    private final WordServiceInterface wordService;
    private final SynonymServiceApi synonymServiceApi;
    private final WordMapper wordMapper;

    @Autowired
    PhraseRepository phraseRepository;

    public WordController(WordServiceInterface wordService, SynonymServiceApi synonymServiceApi, WordMapper wordMapper) {
        this.wordService = wordService;
        this.synonymServiceApi = synonymServiceApi;
        this.wordMapper = wordMapper;
    }

    @GetMapping("/find")
    public ResponseEntity<List<WordFrontEnd>> findWord(@RequestParam String prefix) {

        List<Word> allWordsByPrefix = wordService.getAllWordsByPrefix(prefix);

        // TODO: 20.11.2022 necessary to use mapping from PART to String for Synonyms - because of Synonym RECURSION
        List<WordFrontEnd> wordResponse = allWordsByPrefix.stream().map(wordMapper::toWordRequest).toList();

        return new ResponseEntity<>(wordResponse, HttpStatus.OK);
    }

    @PutMapping("/new")
    public ResponseEntity<?> newWord(@RequestBody WordFrontEnd wordFrontEnd) {
        Word word = wordMapper.mapRequestToWordSkippingSynonyms(wordFrontEnd);

        wordService.saveNewWord(word);
        synonymServiceApi.coupleSynonymsForNewWordFromRequest(wordFrontEnd, word);

        return new ResponseEntity<>(HttpStatus.OK);
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
