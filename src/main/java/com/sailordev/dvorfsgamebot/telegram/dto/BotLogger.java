package com.sailordev.dvorfsgamebot.telegram.dto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotLogger {
    public static void info(String text, String chatId) {
        log.info("Bot : Ответ для {} - {}: \n", chatId, text);
    }
}
