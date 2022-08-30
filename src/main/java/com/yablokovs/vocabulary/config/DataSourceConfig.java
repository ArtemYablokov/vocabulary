package com.yablokovs.vocabulary.config;


import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableConfigurationProperties({DataSourceProperties.class, HibernateProperties.class})
public class DataSourceConfig {

    @Bean
    DataSource getPrimaryDataSource(@Autowired DataSourceProperties dataBaseProperties) {
        return DataSourceBuilder
                .create()
                .username(dataBaseProperties.getUsername())
                .password(dataBaseProperties.getPassword())
                .url(dataBaseProperties.getUrl())
                .driverClassName(dataBaseProperties.getDriverClassName())
                .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired HibernateProperties hibernateProperties,
                                                                @Autowired DataSourceProperties dataBaseProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(getPrimaryDataSource(dataBaseProperties));
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPackagesToScan("com.yablokovs.vocabulary");

        Properties jpaProperties = new Properties();

        //Configures the used database dialect. This allows Hibernate to create SQL
        //that is optimized for the used database.
//        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        //Specifies the action that is invoked to the database when the Hibernate
        //SessionFactory is created or closed.
        jpaProperties.put("hibernate.hbm2ddl.auto", hibernateProperties.getHbm2ddlAuto());

        //Configures the naming strategy that is used when Hibernate creates
        //new database objects and schema elements
//        jpaProperties.put("hibernate.ejb.naming_strategy", env.getRequiredProperty("hibernate.ejb.naming_strategy"));

        //If the value of this property is true, Hibernate writes all SQL
        //statements to the console.
        jpaProperties.put("hibernate.show_sql", hibernateProperties.getShowSql());

        //If the value of this property is true, Hibernate will format the SQL
        //that is written to the console.
//        jpaProperties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));

        //2nd LEVEL CACHE
//        jpaProperties.put("hibernate.cache.use_second_level_cache", true));
//        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory"));
        // + dependency
        // <groupId>org.hibernate</groupId>
        // <artifactId>hibernate-ehcache</artifactId>

        em.setJpaProperties(jpaProperties);
        return em;
    }
}
