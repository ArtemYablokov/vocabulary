package com.yablokovs.vocabulary.config;

import lombok.Getter;
import lombok.Setter;import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "application.hibernate", ignoreUnknownFields = false)
public class HibernateProperties {
    private String hbm2ddlAuto;
    private String showSql;
    private String formatSql;
}
