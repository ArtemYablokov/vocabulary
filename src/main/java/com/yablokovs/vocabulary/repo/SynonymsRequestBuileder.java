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
public class SynonymsRequestBuileder {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DataSource dataSource;



    // TODO: 31.10.2022 move to SynonymsRepo
    @SneakyThrows
//    @Transactional why connection.prepareStatement doesn’t require @Transaction while EntityManager Does, even with INSERT
    // because each statement executed in separate @Transaction
    public Set<Long> findSynonymsOrAntonymsByPartId(Long id, DatabaseName databaseName) {

        Set<Long> ids = new HashSet<>();
        String other_id = getOther_id(databaseName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + other_id + " FROM " + databaseName.name + " WHERE part_id = " + id);
             PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT part_id FROM " + databaseName.name + " WHERE " + other_id + " = " + id);
             ResultSet resultSet = preparedStatement.executeQuery();
             ResultSet resultSet2 = preparedStatement2.executeQuery();
        ) {
            while (resultSet.next()) {
                ids.add(resultSet.getLong(other_id));
            }
            while (resultSet2.next()) {
                ids.add(resultSet2.getLong("part_id"));
            }
        }
        return ids;
    }

    private String getOther_id(DatabaseName databaseName) {
        return databaseName == DatabaseName.SYNONYM ? "synonym_id" : "antonym_id";
    }

    @Transactional // javax.persistence.TransactionRequiredException: Executing an update/delete query
    public void createReference(Long id, Long childId, DatabaseName databaseName) {

        String other_id = getOther_id(databaseName);
        String sqlString = "INSERT INTO " + databaseName.name + " (" + other_id + ", part_id) VALUES ('" + id + "', '" + childId + "')";
        Query nativeQuery = entityManager.createNativeQuery(
                sqlString);
        nativeQuery.executeUpdate();

        Query nativeQuery2 = entityManager.createNativeQuery(
                "INSERT INTO " + databaseName.name + " (" + other_id + ", part_id) VALUES ('" + childId + "', '" + id + "')");
        nativeQuery2.executeUpdate();

//        entityManager.flush();
    }

    public enum DatabaseName {
        SYNONYM("part_synonym"), ANTONYM("part_antonym");

        public final String name;

        private DatabaseName(String name) {
            this.name = name;
        }
    }
}
