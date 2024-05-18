package com.sailordev.dvorfsgamebot.telegram.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@PropertySource("application.yaml")
@Data
public class BotProperties {
    private String name;
    private String token;
}
