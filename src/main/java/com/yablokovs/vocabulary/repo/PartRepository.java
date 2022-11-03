package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Part;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class PartRepository {

    //    @Query(nativeQuery = true, value = "INSERT INTO part (name, word_id) VALUES ('" + partOfSpeech + "', '" + wordId + "') ")
//    @Lock(LockModeType.NONE)
    public Long addPartToWord(String partOfSpeech, Long wordId) {
        return 0L;
    }

    // TODO: 31.10.2022 move to SynonymsRepo
    public Set<Long> findSynonymsByPartId(Long ids) {
        return Collections.emptySet();
    }

    public void createReference(Long id, Long childId) {
    }

    public void save(Part part) {
    }

    public List<Part> findAll() {
        return Collections.emptyList();
    }
}
