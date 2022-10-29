package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Word;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
// TODO: 15.09.2022 WHAT IS THE DIFFERENCE import org.springframework.data.jpa.repository.JpaRepository;
// findAll in Jpa return list VS CRUD return iterable

@Repository // TODO: 22.10.2022 try to remove
public interface WordRepository extends CrudRepository<Word, Long> {
    Optional<Word> findByName(String name);

    List<Word> findAll();
}
