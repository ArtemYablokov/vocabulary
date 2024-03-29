package com.yablokovs.vocabulary.rest;

import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.mdto.request.mapper.WordMapper;
import com.yablokovs.vocabulary.model.Word;
import com.yablokovs.vocabulary.service.ExternalService;
import com.yablokovs.vocabulary.service.SynonymServiceConductor;
import com.yablokovs.vocabulary.service.WordServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
public class WordController {

    // TODO: 22.10.2022 с помощью имени бина можно задавать имплементацию (кроме @Primary и @Qualifier)
    private final WordServiceInterface wordService;
    private final SynonymServiceConductor synonymServiceConductor;
    private final WordMapper wordMapper;

    private final ExternalService externalService;

    public WordController(WordServiceInterface wordService, SynonymServiceConductor synonymServiceConductor, WordMapper wordMapper, ExternalService externalService) {
        this.wordService = wordService;
        this.synonymServiceConductor = synonymServiceConductor;
        this.wordMapper = wordMapper;
        this.externalService = externalService;
    }

    @GetMapping("/find")
    public ResponseEntity<List<WordFrontEnd>> findWord(@RequestParam String prefix) {

        List<Word> allWordsByPrefix = wordService.getAllWordsByPrefix(prefix.trim());

        // TODO: 20.11.2022 necessary to use mapping from PART to String for Synonyms - because of Synonym RECURSION
        List<WordFrontEnd> wordResponse = allWordsByPrefix.stream().map(wordMapper::toWordResponse).toList();

        // search external
//        WordFrontEnd word = externalService.findWord(prefix);

        return new ResponseEntity<>(wordResponse, HttpStatus.OK);
    }

    @PutMapping("/new")
    public ResponseEntity<?> newWord(@RequestBody WordFrontEnd wordFrontEnd) {
        Word word = wordMapper.mapRequestToWordIgnoreSynonymsAndAntonyms(wordFrontEnd);

        wordService.saveNewWord(word);

        synonymServiceConductor.coupleSynAndAntNewImplementation(wordFrontEnd, word);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<HttpStatus> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
