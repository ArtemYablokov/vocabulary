package com.yablokovs.vocabulary.repo;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

@Repository
public class SynonymsRepo {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DataSource dataSource;

    // TODO: 31.10.2022 move to SynonymsRepo
    @SneakyThrows
//    @Transactional why connection.prepareStatement doesn’t require @Transaction while EntityManager Does, even with INSERT
    // because each statement executed in separate @Transaction
    public Set<Long> findSynonymsByPartId(Long id) {

        Set<Long> ids = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT synonym_id FROM part_synonym WHERE part_id = " + id);
             PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT part_id FROM part_synonym WHERE synonym_id = " + id);
             ResultSet resultSet = preparedStatement.executeQuery();
             ResultSet resultSet2 = preparedStatement2.executeQuery();
        ) {
            while (resultSet.next()) {
                ids.add(resultSet.getLong("synonym_id"));
            }
            while (resultSet2.next()) {
                ids.add(resultSet2.getLong("part_id"));
            }
        }
        return ids;
    }
    @Transactional // javax.persistence.TransactionRequiredException: Executing an update/delete query
    public void createReference(Long id, Long childId) {

        Query nativeQuery = entityManager.createNativeQuery(
                "INSERT INTO part_synonym (synonym_id, part_id) VALUES ('" + id + "', '" + childId + "')");
        nativeQuery.executeUpdate();
//        entityManager.flush();
    }
}
