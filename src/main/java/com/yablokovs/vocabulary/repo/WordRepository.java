package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // TODO: 22.10.2022 try to remove
public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByName(String name);
}
