package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.WordRus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface WordRusRepository extends JpaRepository<WordRus, Long> {

    Set<WordRus> findAllByNameIn(Collection<String> synonyms);
}
