package com.sailordev.dvorfsgamebot.telegram.dto;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    SendMessage sendCommandMessage(UserEntity user);
    String getName();
    String getDescription();
}
