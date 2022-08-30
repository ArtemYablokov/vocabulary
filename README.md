# VOCABULARY

### H2 not embeded - but TCP

url:
jdbc:h2:tcp://localhost:9092/mem:mydb
sa
password


### HIBERNATE
application.hibernate.hbm2ddl.auto

- *validate*: validate the schema, makes no changes to the database.
- *update*: update the schema.
- *create*: creates the schema, destroying previous data.
- *create-drop*: drop the schema when the SessionFactory is closed explicitly, typically when the application is stopped.
- *none*: does nothing with the schema, makes no changes to the database

by default = none

2nd Level Cache can be enabled in:
src/main/java/com/yablokovs/vocabulary/config/DataSourceConfig.java

### Liquibase
Both comands not overwrite files!
- generate changeLog SCHEMA command:
  mvn liquibase:generateChangeLog

- generate changeLog DATA command:
mvn liquibase:generateChangeLog -Dliquibase.diffTypes=data


- generate diff between Entity and DB state (can use JPA-buddy)
??? setup liqui-hibernate plugin

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)