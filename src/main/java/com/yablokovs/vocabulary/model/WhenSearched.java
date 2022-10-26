package com.yablokovs.vocabulary.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class WhenSearched {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column
    Timestamp createdAt;
}
