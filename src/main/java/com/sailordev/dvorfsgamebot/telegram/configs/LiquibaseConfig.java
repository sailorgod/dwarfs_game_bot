package com.sailordev.dvorfsgamebot.telegram.configs;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource("application.yaml")
@ConfigurationProperties(prefix = "liquibase")
public class LiquibaseConfig {

    @Value("$change-log")
    private String changeLog;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties liquibaseProperties){
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(liquibaseProperties.getChangeLog());
        return springLiquibase;
    }

    @Bean
    public LiquibaseProperties liquibaseProperties() {
        LiquibaseProperties liquibaseProperties = new LiquibaseProperties();
        liquibaseProperties.setChangeLog(changeLog);
        return liquibaseProperties;
    }
}
