package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Word;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
// TODO: 15.09.2022 WHAT IS THE DIFFERENCE import org.springframework.data.jpa.repository.JpaRepository;

@Repository // TODO: 22.10.2022 try to remove
public interface WordRepository extends CrudRepository<Word, Long> {
    Optional<Word> findByName(String name);
    List<Word> findAll();
}
