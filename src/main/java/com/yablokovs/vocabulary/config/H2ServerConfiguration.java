package com.yablokovs.vocabulary.config;

import lombok.SneakyThrows;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * For accessing H2 from outside java
 * */
@Configuration
public class H2ServerConfiguration {

//jdbc:h2:tcp://localhost:9092/mem:testdb

    @SneakyThrows
    @Bean
    public Server createH2Server() {
        Server tcpServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
        return tcpServer.start();
    }
}
