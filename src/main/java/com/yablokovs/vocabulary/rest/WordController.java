package com.yablokovs.vocabulary.rest;

import com.yablokovs.vocabulary.mdto.request.WordRequest;
import com.yablokovs.vocabulary.mdto.request.mapper.WordMapper;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.repo.PhraseRepository;
import com.yablokovs.vocabulary.service.PrefixService;
import com.yablokovs.vocabulary.service.SynonymServiceApi;
import com.yablokovs.vocabulary.service.WordServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
public class WordController {

    // TODO: 22.10.2022 с помощью имени бина можно задавать имплементацию (кроме @Primary и @Qualifier)
    private final WordServiceInterface wordService;
    private final SynonymServiceApi synonymServiceApi;
    private final PrefixService prefixService;
    private final WordMapper wordMapper;

    @Autowired
    PhraseRepository phraseRepository;

    public WordController(WordServiceInterface wordService, SynonymServiceApi synonymServiceApi, PrefixService prefixService, WordMapper wordMapper) {
        this.wordService = wordService;
        this.synonymServiceApi = synonymServiceApi;
        this.prefixService = prefixService;
        this.wordMapper = wordMapper;
    }

    @GetMapping("/find")
    public ResponseEntity<List<Word>> findWord(@RequestParam String prefix) {

        List<Word> allWordsByPrefix = wordService.getAllWordsByPrefix(prefix);

        // TODO: 20.11.2022 not necessary to use mapping from PART to String for Synonyms
//        List<WordRequest> wordRequests = allWordsByPrefix.stream().map(wordMapper::toWordRequest).collect(Collectors.toList());

        return new ResponseEntity<>(allWordsByPrefix, HttpStatus.OK);
    }

    @PutMapping("/new")
    public ResponseEntity<?> newWord(@RequestBody WordRequest wordRequest) {
        Word word = wordMapper.toWord(wordRequest);
        wordService.saveNewWordWithPartsAndDefinitions(word);

        synonymServiceApi.coupleSynonyms(wordRequest, word);

        prefixService.synchronisePrefixesForWordWithoutAddingWordToPrefixSet(word);
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
