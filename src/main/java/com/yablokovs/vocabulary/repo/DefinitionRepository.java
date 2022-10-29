package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Definition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefinitionRepository extends JpaRepository<Definition, Long> {

}
