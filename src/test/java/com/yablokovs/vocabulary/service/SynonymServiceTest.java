package com.yablokovs.vocabulary.service;

import com.yablokovs.vocabulary.mdto.request.PartDto;
import com.yablokovs.vocabulary.mdto.request.StringHolder;
import com.yablokovs.vocabulary.mdto.request.WordFrontEnd;
import com.yablokovs.vocabulary.repo.SynonymsRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;


@SpringBootTest
class SynonymServiceTest {
    @MockBean
    SynonymsRepo synonymsRepo;

    @Autowired
    SynonymService synonymService;

//    @BeforeEach
//    void init() {
//        wordRepository.save(new Word());
//    }

    @AfterEach
    void tearDown() {
    }

    // TODO: 03.11.2022 empty corner case
    @Test
    void filterExistedSynonymsToUniqueSetsShouldReturnSetsOfUniqueSynonyms() {
        Set<String> partOfSpeeches = new HashSet<>(Set.of("verb"));

        Map<String, List<Long>> partOfSpeechToExistedSynonymAsPartId = new HashMap<>();
        List<Long> existedSynonymsIds = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        partOfSpeechToExistedSynonymAsPartId.put("verb", existedSynonymsIds);

        Map<Long, Set<Long>> existedSynonymToItsSynonym = new HashMap<>();
        existedSynonymToItsSynonym.put(1L, new HashSet<>(Set.of(2L, 12L)));
        existedSynonymToItsSynonym.put(2L, new HashSet<>(Set.of(1L, 12L)));
        existedSynonymToItsSynonym.put(3L, new HashSet<>(Set.of(5L, 6L, 32L)));
        existedSynonymToItsSynonym.put(4L, new HashSet<>(Set.of(41L, 42L)));
        existedSynonymToItsSynonym.put(5L, new HashSet<>(Set.of(3L, 6L, 32L)));
        existedSynonymToItsSynonym.put(6L, new HashSet<>(Set.of(3L, 5L, 32L)));
        existedSynonymToItsSynonym.forEach((key, set) -> {
//            Mockito.when(synonymsRepo.findSynonymsByPartId(key)).thenReturn(existedSynonymToItsSynonym.get(key));
        });

//        Map<String, List<Set<Long>>> actual = synonymService.filterExistedSynonymsToUniqueSets(partOfSpeeches, partOfSpeechToExistedSynonymAsPartId);

        Map<String, List<Set<Long>>> expected = new HashMap<>();
        expected.put("verb", List.of(Set.of(1L, 2L, 12L), Set.of(3L, 5L, 6L, 32L), Set.of(4L, 41L, 42L)));

//        Assertions.assertEquals(expected, actual);
    }

    @Test
    void preparePartToSynonymMap() {
        WordFrontEnd wordFrontEnd = new WordFrontEnd();
        PartDto partDto1 = new PartDto("verb", List.of(new StringHolder("verb1"), new StringHolder("verb2"), new StringHolder("verb3")));
        PartDto partDto2 = new PartDto("noun", List.of(new StringHolder("noun1"), new StringHolder("noun2")));

        wordFrontEnd.setParts(List.of(partDto1, partDto2));
        Map<String, Set<String>> actual = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, PartDto::getSynonyms);


        Map<String, List<String>> expected = new HashMap<>(
                Map.of("verb", List.of("verb1", "verb2", "verb3"), "noun", List.of("noun1", "noun2")));

        Assertions.assertEquals(expected, actual);

    }

    @Test
    void preparePartToSynonymMapEmpty() {
        WordFrontEnd wordFrontEnd = new WordFrontEnd();
        wordFrontEnd.setParts(Collections.emptyList());
        Map<String, Set<String>> actual = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, PartDto::getSynonyms);

        Map<String, List<String>> expected = new HashMap<>();

        // TODO: 24/02/23 because of both maps are empty - there was not TYPE assigned yet
        
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void prepareMapEmpty() {
        WordFrontEnd wordFrontEnd = new WordFrontEnd();

        PartDto partDto1 = new PartDto("verb", new ArrayList<>());


        wordFrontEnd.setParts(List.of(partDto1));
        Map<String, Set<String>> actual = synonymService.getAllSynOrAntStringSortedByPartOfSpeech(wordFrontEnd, PartDto::getSynonyms);

        Map<String, List<String>> expected = new HashMap<>();

        // TODO: 24/02/23 because of both maps are empty - there was not TYPE assigned yet

        Assertions.assertEquals(expected, actual);
    }



    // TODO: 03.11.2022 move to NON-Spring tests because no context required
    @Test
    void getExistedSynonymsPairsToBeCoupled() {
        Map<String, List<Set<Long>>> uniqueSetsOfExistingSets = new HashMap<>();

        List<Set<Long>> sets = new ArrayList<>();
        sets.add(Set.of(1L, 2L, 12L));
        sets.add(Set.of(3L, 5L));
        sets.add(Set.of(4L, 41L));
        uniqueSetsOfExistingSets.put("verb", sets);

        Set<IdTuple> actual = synonymService.getPairsOfExistedSynonymsToBeCoupled(uniqueSetsOfExistingSets);

        Set<IdTuple> expected = new HashSet<>(
                Set.of(new IdTuple(1L, 3L),
                        new IdTuple(1L, 5L),
                        new IdTuple(1L, 4L),
                        new IdTuple(1L, 41L),

                        new IdTuple(2L, 3L),
                        new IdTuple(2L, 5L),
                        new IdTuple(2L, 4L),
                        new IdTuple(2L, 41L),

                        new IdTuple(12L, 3L),
                        new IdTuple(12L, 5L),
                        new IdTuple(12L, 4L),
                        new IdTuple(12L, 41L),

                        new IdTuple(3L, 4L),
                        new IdTuple(3L, 41L),

                        new IdTuple(5L, 4L),
                        new IdTuple(5L, 41L)
                ));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getExistedSynonymsToBeCoupledWithNew() {
        Map<String, List<Set<Long>>> partToExistedSynonymsUniqueSets = new HashMap<>();
        partToExistedSynonymsUniqueSets.put("verb", List.of(new HashSet<>(Set.of(1L, 2L, 3L)), new HashSet<>(Set.of(5L, 6L, 7L)), new HashSet<>(Set.of(11L, 12L, 13L))));
        partToExistedSynonymsUniqueSets.put("noun", List.of(new HashSet<>(Set.of(21L, 22L)), new HashSet<>(Collections.singleton(23L)), new HashSet<>(Set.of(31L, 32L, 33L))));

        Map<String, Set<Long>> actual = synonymService.getExistedSynonymsToBeCoupledWithNew(partToExistedSynonymsUniqueSets);

        Map<String, Set<Long>> expected = new HashMap<>();
        expected.put("verb", Set.of(1L, 2L, 3L,5L, 6L, 7L, 11L, 12L, 13L));
        expected.put("noun", Set.of(21L, 22L, 23L, 31L, 32L, 33L));

        Assertions.assertEquals(expected, actual);
    }
}
