package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class AddCoordinatesCommand implements Command {

    private final UserRepository userRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "Введите координаты нового гнома.";
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        BotLogger.info(text, chatId);
        user.setState(UserState.AWAIT_ADD_COORDINATES);
        userRepository.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Добавить координаты";
    }

    @Override
    public String getDescription() {
        return "Добавляет новые координаты гнома";
    }
}
