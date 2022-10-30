package com.yablokovs.vocabulary.repo;

import com.yablokovs.vocabulary.model.Prefix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrefixRepository extends JpaRepository<Prefix, Long> {
    Optional<Prefix> findByName(String prefix);
}
