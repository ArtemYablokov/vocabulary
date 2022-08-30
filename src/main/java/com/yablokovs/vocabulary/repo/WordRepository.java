package com.yablokovs.vocabulary.repo;


import com.yablokovs.vocabulary.model.Word;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findByName(String name);
}
