package com.yablokovs.vocabulary.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.datasource", ignoreUnknownFields = false)
public class DataSourceProperties {
    private String username;
    private String password;
    private String url;
    private String driverClassName;
}
