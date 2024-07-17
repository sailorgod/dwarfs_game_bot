package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEventCommand implements Command {

    @Autowired
    private final UserRepository userRepository;
    private static final String NAME = "Создать ивент";
    private static final String CREATE_EVENT_DESCRIPTION = "Создать новый ивент";

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        user.setState(UserState.AWAIT_SET_EVENT_DATE);
        userRepository.save(user);
        String text = "Отлично. Введите дату и время начала ивента в формате - дд.мм.гггг чч:мм";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return CREATE_EVENT_DESCRIPTION;
    }
}
