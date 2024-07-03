package com.sailordev.dvorfsgamebot.telegram.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "admin")
@PropertySource("application.yaml")
@Data
public class AdminUserProperty {
    String chatId;
    @Bean
    public String adminChatId() {
        return chatId;
    }
}
