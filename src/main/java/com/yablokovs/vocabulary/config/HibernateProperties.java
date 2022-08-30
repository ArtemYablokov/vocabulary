package com.yablokovs.vocabulary.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.hibernate", ignoreUnknownFields = false)
public class HibernateProperties {
    private String hbm2ddlAuto;
    private String showSql;
}
