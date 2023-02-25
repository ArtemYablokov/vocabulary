package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Word;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByName(String name);

    // TODO: 25/02/23 check if it returns only PARTS (not PART's children)
    @EntityGraph(attributePaths = {"parts"})
    Set<Word> findAllByNameIn(Set<String> synonyms);
}
