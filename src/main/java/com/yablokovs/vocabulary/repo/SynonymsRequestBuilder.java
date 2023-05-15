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
public class SynonymsRequestBuilder {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DataSource dataSource;


    // TODO: 31.10.2022 move to SynonymsRepo
    @SneakyThrows
    // @Transactional why connection.prepareStatement doesn’t require @Transaction while EntityManager Does, even with INSERT
    // because each statement executed in separate @Transaction
    public Set<Long> findSynonymsOrAntonymsByPartId(Long id, DatabaseName databaseName) {
        Set<Long> idsResult = new HashSet<>();

        String sql1 = "SELECT " + databaseName.leftColumn + " FROM " + databaseName.name + " WHERE " + databaseName.rightColumn + " = " + id;
        String sql2 = "SELECT " + databaseName.rightColumn + " FROM " + databaseName.name + " WHERE " + databaseName.leftColumn + " = " + id;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql1);
             ResultSet resultSet = preparedStatement.executeQuery();

             PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
             ResultSet resultSet2 = preparedStatement2.executeQuery();
        ) {
            while (resultSet.next()) {
                idsResult.add(resultSet.getLong(databaseName.leftColumn));
            }
            while (resultSet2.next()) {
                idsResult.add(resultSet2.getLong(databaseName.rightColumn));
            }
        }
        return idsResult;
    }

    @Transactional // javax.persistence.TransactionRequiredException: Executing an update/delete query
    public void createReference(Long id, Long childId, DatabaseName databaseName) {

        String sqlString = "INSERT INTO " + databaseName.name + " (" + databaseName.leftColumn + ", " + databaseName.rightColumn + ") VALUES ('" + id + "', '" + childId + "')";
        Query nativeQuery = entityManager.createNativeQuery(sqlString);
        nativeQuery.executeUpdate();

        String sqlString2 = "INSERT INTO " + databaseName.name + " (" + databaseName.leftColumn + ", " + databaseName.rightColumn + ") VALUES ('" + childId + "', '" + id + "')";
        Query nativeQuery2 = entityManager.createNativeQuery(sqlString2);
        nativeQuery2.executeUpdate();

//        entityManager.flush();
    }

    @SneakyThrows
    public Set<Long> findEngPartSynOrAntIdsByRusWordId(Long wordRusId, DatabaseName databaseName) {
        Set<Long> idsResult = new HashSet<>();

        String sql1 = "SELECT " + databaseName.leftColumn + " FROM " + databaseName.name + " WHERE " + databaseName.rightColumn + " = " + wordRusId;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql1);
             ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            while (resultSet.next()) {
                idsResult.add(resultSet.getLong(databaseName.leftColumn));
            }
        }
        return idsResult;
    }

    @Transactional
    public void createReferenceWordRusToEngPartAsSynOrAnt(Long wordRusId, Long partEngId, DatabaseName databaseName) {

        String sqlString = "INSERT INTO " + databaseName.name + " (" + databaseName.leftColumn + ", " + databaseName.rightColumn + ") VALUES ('" + partEngId + "', '" + wordRusId + "')";
        Query nativeQuery = entityManager.createNativeQuery(sqlString);
        nativeQuery.executeUpdate();
    }

    public enum DatabaseName {
        ENG_SYNONYM("part_synonym", "part_id", "synonym_id"),
        ENG_ANTONYM("part_antonym", "part_id", "antonym_id"),
        RUS_SYNONYM("rus_synonym", "word_rus_id", "synonym_rus_id"),
        RUS_ANTONYM("rus_antonym", "word_rus_id", "antonym_rus_id"),
        RUS_ENG_SYNONYM("rus_eng_synonym", "part_eng_id", "word_rus_id"),
        RUS_ENG_ANTONYM("rus_eng_antonym", "part_eng_id", "word_rus_id");


        public final String name;
        public final String leftColumn;
        public final String rightColumn;

        DatabaseName(String name, String leftColumn, String rightColumn) {
            this.name = name;
            this.leftColumn = leftColumn;
            this.rightColumn = rightColumn;
        }
    }

}
