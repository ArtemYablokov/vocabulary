package com.yablokovs.vocabulary.config;

import java.util.Properties;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableConfigurationProperties({DataSourceProperties.class, HibernateProperties.class})
// TODO: 16.10.2022 remove to TEST config class
@ComponentScan({"com.yablokovs.vocabulary"})
@EnableJpaRepositories(basePackages = "com.yablokovs.vocabulary.repo")
@EnableTransactionManagement
public class DataSourceConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;

    @Autowired
    HibernateProperties hibernateProperties;

    @Bean
    DataSource getPrimaryDataSource() {
        // TODO: 16.10.2022 remove to TEST config class
        setHardcodedPropertiesBecauseSpringDoesntSupportConfigurationProperty();

        return DataSourceBuilder
                .create()
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .url(dataSourceProperties.getUrl())
                .driverClassName(dataSourceProperties.getDriverClassName())
                .build();
    }

    private void setHardcodedPropertiesBecauseSpringDoesntSupportConfigurationProperty() {
        dataSourceProperties.setPassword("password");
        dataSourceProperties.setDriverClassName("org.h2.Driver");
        dataSourceProperties.setUrl("jdbc:h2:mem:mydb");
        dataSourceProperties.setUsername("sa");
    }

    @SneakyThrows
    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        // TODO: 16.10.2022 remove to TEST config class
        setHibernatePropertiesBecauseSpringDoesntSupportConfigurationProperty();

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(getPrimaryDataSource());
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPackagesToScan("com.yablokovs.vocabulary");
        em.setJpaProperties(prepareJpaProperties(hibernateProperties));
        return em;
    }

    private void setHibernatePropertiesBecauseSpringDoesntSupportConfigurationProperty() {
        hibernateProperties.setShowSql("true");
        hibernateProperties.setHbm2ddlAuto("update");
    }

    // TODO: 16.10.2022 remove to TEST config class - looks like necessary only for test context
    // with it, we can use declarative @Transactional
//    @Bean
    JpaTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return jpaTransactionManager;
    }

    private Properties prepareJpaProperties(HibernateProperties hibernateProperties) {
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
        jpaProperties.put("hibernate.format_sql", hibernateProperties.getFormatSql());

//        spring.jpa.properties.hibernate.format_ sql=true

        //If the value of this property is true, Hibernate will format the SQL
        //that is written to the console.
//        jpaProperties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));

        //2nd LEVEL CACHE
//        jpaProperties.put("hibernate.cache.use_second_level_cache", true));
//        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory"));
        // + dependency
        // <groupId>org.hibernate</groupId>
        // <artifactId>hibernate-ehcache</artifactId>
        return jpaProperties;
    }
}
