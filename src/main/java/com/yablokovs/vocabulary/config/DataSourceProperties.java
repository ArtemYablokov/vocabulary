package com.yablokovs.vocabulary.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.postgres.datasource", ignoreUnknownFields = false)
public class DataSourceProperties {
    private String username;
    private String password;
    private String url;
    private String driverClassName;
}
