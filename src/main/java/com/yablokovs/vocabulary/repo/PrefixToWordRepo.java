package com.yablokovs.vocabulary.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class PrefixToWordRepo {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void addWordsToPrefix(long prefixId, long wordId) {
        // TODO: 29.10.2022 configure SQL dialect in config
        Query nativeQuery = entityManager.createNativeQuery(
                "INSERT INTO prefixes_words (prefix_id, word_id) VALUES ('" + prefixId + "', '" + wordId + "')");
        nativeQuery.executeUpdate();
    }
}
