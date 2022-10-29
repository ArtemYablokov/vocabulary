package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {

}
