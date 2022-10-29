package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {

}
