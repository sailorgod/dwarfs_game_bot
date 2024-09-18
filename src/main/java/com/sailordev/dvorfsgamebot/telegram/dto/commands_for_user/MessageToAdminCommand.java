package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.handlers.user.MessageToAdminHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class MessageToAdminCommand implements Command {

    private final UserRepository userRepository;
    private final MessageToAdminHandler messageToAdminHandler;
    private static final String MESSAGE_TO_ADMIN_DESCRIPTION = "Связь с администратором";

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        if(user.getState().equals(UserState.AWAIT_ADMINS_RESPONSE)) {
            return messageToAdminHandler.sendAnswerToUser(user);
        }
        String text = "Введите текст вашего сообщения администратору: ";
        String chatId = user.getUserChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        user.setState(UserState.AWAIT_MESSAGE_TO_ADMIN);
        userRepository.save(user);
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return MESSAGE_TO_ADMIN_DESCRIPTION;
    }

    @Override
    public String getDescription() {
        return MESSAGE_TO_ADMIN_DESCRIPTION;
    }
}
